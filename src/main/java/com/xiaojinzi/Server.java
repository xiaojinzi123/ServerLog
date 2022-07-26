package com.xiaojinzi;

import com.google.gson.Gson;
import com.xiaojinzi.anno.NotEmpty;
import com.xiaojinzi.anno.NotNull;
import com.xiaojinzi.anno.ThreadSafe;
import com.xiaojinzi.anno.ThreadUnSafe;
import com.xiaojinzi.bean.Message;
import com.xiaojinzi.bean.MessageFragment;
import com.xiaojinzi.util.MessageJsonUtil;
import com.xiaojinzi.util.Strings;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Server implements Runnable {

    public static final String UID = "Server_" + UUID.randomUUID().toString();
    public static final String TAG = "Server";

    public static final Message.Owner owner = new Message.Owner(UID, TAG);

    private static Server instance = new Server();

    private final Gson g = new Gson();

    @ThreadUnSafe
    private final List<String> messageQueue = new LinkedList<>();

    @ThreadSafe
    private final CopyOnWriteArrayList<Client> clientList = new CopyOnWriteArrayList<>();

    private Server() {
        // 用于发送数据
        new Thread(this).start();
    }

    @ThreadSafe
    public static Server getInstance() {
        return instance;
    }

    @Override
    public void run() {
        long preTime = System.currentTimeMillis();
        while (true) {
            try {
                // 如果以前的时间超过五秒了, 则发送一个心跳包
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - preTime > 3 * 1000) {
                    synchronized (messageQueue) {
                        messageQueue.add(g.toJson(Message.heartbeatMessage()));
                    }
                    preTime = currentTimeMillis;
                }
                if (messageQueue.isEmpty()) {
                    Thread.sleep(500);
                    continue;
                }
                synchronized (messageQueue) {
                    if (messageQueue.isEmpty()) {
                        continue;
                    } else {
                        String message = messageQueue.remove(0);
                        doForward(message);
                    }
                }
            } catch (Exception ignore) {
                // ignore
            }
        }
    }

    /**
     * 转发消息
     */
    @ThreadUnSafe
    public boolean forward(@NotEmpty String message) {
        try {
            JSONObject jb = new JSONObject(message);
            String type = jb.optString(Message.ATTR_TYPE);
            // 如果消息的必须的参数不足, 则被忽略
            if (Strings.isEmpty(type) || MessageJsonUtil.isOwnerInvalid(jb)) {
                return false;
            }
            // 如果是心跳包, 则忽略
            if (Message.TYPE_HEARTBEAT.equals(type)) {
                return false;
            }
            // 如果是分片的数据, 需要加到数据缓存中
            if (Message.TYPE_DATA_FRAGMENT.equals(type)) {
                MessageFragment messageFragment = g.fromJson(message, MessageFragment.class);
                return DataFragment.INSTANCE.addData(messageFragment);
            } else {
                synchronized (messageQueue) {
                    messageQueue.add(message);
                }
                return true;
            }
        } catch (Exception ignore) {
            return false;
        }
    }

    @ThreadUnSafe
    private void doForward(@NotEmpty final String message) {
        try {
            JSONObject jb = new JSONObject(message);
            String type = jb.optString(Message.ATTR_TYPE);
            List<Client> clients = null;
            // 如果是心跳, 就是所有的 Client
            if (Message.TYPE_HEARTBEAT.equals(type)) {
                clients = new ArrayList<>(clientList);
            } else {
                clients = filterClientBySubscribeType(type);
            }
            clients.forEach(client -> {
                client.send(message);
            });
        } catch (Exception ignore) {
            // ignore
        }
    }

    /**
     * 过滤出想要某个数据类型的所有的 Client
     */
    @NotEmpty
    @ThreadSafe
    private synchronized List<Client> filterClientBySubscribeType(@NotEmpty String type) {
        return clientList.stream()
                .filter(client -> client.isSubscribe(type))
                .collect(Collectors.toList());
    }

    /**
     * 每一个 Client 都可能有感兴趣的数据类型和想要订阅的数据类型
     * 这个方法会把 Client 感兴趣的数据类型的提供者的所有 Client 的信息给发送过去
     */
    @ThreadSafe
    public synchronized void sendClientInfo() {

        // key 为某一个数据类型的提供, 比如 network
        // value 为提供 key 这种数据类型的 Client 的信息
        Map<String, List<Message.Owner>> map = new HashMap<>();
        clientList.forEach(client -> {
            Set<String> providerTypes = client.getProviderTypes();
            for (String providerType : providerTypes) {
                String key = Message.TYPE_PROVIDER_LIST + Message.AI_TE + providerType;
                List<Message.Owner> clients = map.getOrDefault(key, new ArrayList<>());
                clients.add(client.toOwner());
                map.put(key, clients);
            }
        });

        clientList.forEach(client -> {
            Set<String> subscribeTypes = client.getSubscribeTypes();
            for (String subscribeType : subscribeTypes) {
                // subscribeType 可能为：network, providerList@network
                // 但是只要 providerList@ 开头的
                if (!subscribeType.startsWith(Message.TYPE_PROVIDER_LIST + Message.AI_TE)) {
                    continue;
                }
                List<Message.Owner> clientInfoList = map.getOrDefault(subscribeType, new ArrayList<>());
                Message message = new Message();
                message.setType(subscribeType);
                message.setOwner(toOwner());
                message.setData(clientInfoList);
                forward(g.toJson(message));
            }
        });

    }

    /**
     * 添加了一个 Client
     */
    @ThreadSafe
    public synchronized void addClient(@NotNull Client client) {
        if (!clientList.contains(client)) {
            clientList.add(client);
        }
        sendClientInfo();
    }

    /**
     * 移除了一个 Client
     */
    @ThreadSafe
    public synchronized void removeClient(@NotNull Client client) {
        clientList.remove(client);
        sendClientInfo();
    }

    public Message.Owner toOwner(){
        return owner;
    }

}
