package com.xiaojinzi.socket;


import com.xiaojinzi.Client;
import com.xiaojinzi.anno.AnyThread;
import com.xiaojinzi.anno.NotNull;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.Writer;
import java.util.Vector;

@Component
@ServerEndpoint("/client")
public class WebSocketClient extends Client {

    /**
     * 所有的回话
     */
    private static Vector<Session> sessions = new Vector<>();

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        onAcceptMessage(message);
    }

    /**
     * 当前的会话
     */
    private Session mSession;

    /**
     * 连接建立成功调用的方法
     *
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session) {
        mSession = session;
        sessions.add(session);
        onOpenClient();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        destroy();
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public synchronized void onClose() {
        destroy();
    }

    private void destroy() {
        onCloseClient();
        if (mSession != null) {
            sessions.remove(mSession);
            try {
                mSession.close();
            } catch (Exception ignore) {
                // ignore
            }
        }
    }

    @Override
    @AnyThread
    public synchronized void send(@NotNull String message) {
        if (mSession == null) {
            return;
        }
        try {
            RemoteEndpoint.Basic basicRemote = mSession.getBasicRemote();
            basicRemote.sendText(message);
        } catch (Exception e) {
            destroy();
        }
    }

}
