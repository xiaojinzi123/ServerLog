package com.xiaojinzi.bean;

import com.xiaojinzi.Server;
import com.xiaojinzi.anno.NotEmpty;
import com.xiaojinzi.anno.NotNull;
import com.xiaojinzi.util.Strings;

public class Message<T> {

    public static final String AI_TE = "@";

    /*几种和服务端息息相关的 type, 其他的已经是额外自定义的, 和服务器无关*/

    public static final String TYPE_HEARTBEAT = "heartbeat";
    public static final String TYPE_CLIENT_TAG = "clientTag";
    // 表示消息的是定向发送的类型, 后面需要跟目标的 tag
    public static final String TYPE_DIRECT = "direct";
    // 这个一定是 Client 自己给自己设置名称
    public static final String TYPE_SET_CLIENT_NAME = "setClientName";
    public static final String TYPE_SET_PROVIDER_TYPES = "setProviderTypes";
    public static final String TYPE_SET_SUBSCRIBE_TYPES = "setSubscribeTypes";
    /*这个表示当 Cient 发生变化的时候,
    会把 Client 订阅的数据类型的 数据提供的 Client 信息发送给 数据消费者的 Client*/
    public static final String TYPE_PROVIDER_LIST = "providerList";
    /*表示数据分片传送*/
    public static final String TYPE_DATA_FRAGMENT = "dataFragment";

    public static final String ATTR_OWNER = "owner";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_DATA = "data";

    // 表示自身的 Tag
    private Owner owner;

    // 标记消息的类型
    private String type;

    // 真正发送出去的数据,最终会转化成 json 数据传出去
    private T data;

    @NotNull
    public static Message heartbeatMessage() {
        Message message = new Message();
        message.setType(TYPE_HEARTBEAT);
        message.setOwner(Server.getInstance().toOwner());
        return message;
    }

    @NotNull
    public static Message clientTagMessage(@NotEmpty String tag) {
        Message message = new Message();
        message.setType(TYPE_CLIENT_TAG);
        message.setOwner(Server.getInstance().toOwner());
        message.setData(tag);
        return message;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static class Owner {

        public static final String ATTR_UID = "uid";
        public static final String ATTR_NAME = "name";

        private String uid;
        private String name;

        public Owner(String uid, String name) {
            this.uid = uid;
            this.name = name;
        }
        public String getUid() {
            return uid;
        }
        public void setUid(String uid) {
            this.uid = uid;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }

}