package com.serverlog;

import com.serverlog.dto.DataDto;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 针对 websocket 协议的 Client 实现,每一个连接进来都会产生一个 Client
 */
@Component
@ServerEndpoint(value = "/websocket")
public class WebSocketClient extends Client {

    /**
     * 本连接对应的 Session
     */
    private Session mSession;

    @Override
    protected void doSend(@NotNull DataDto data) throws Exception {
        if (mSession == null) {
            throw new NullPointerException("mSession is null");
        }
        if (!mSession.isOpen()) {
            throw new Exception("websocket is not open");
        }
        // 同步发送消息
        mSession.getBasicRemote().sendText(LogServer.GSON.toJson(data));
    }

    @Override
    public @NotNull List<String> getInterestedType() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void close() {
        setOnMessageListener(null);
        LogServer.getInstance().removeClient(this);
        if (mSession != null) {
            try {
                mSession.close();
            } catch (IOException ignore) {
                // ignore
            }
        }
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        this.mSession = session;
        LogServer.getInstance().addClient(this);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        LogServer.getInstance().removeClient(this);
        this.mSession = null;
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        relsoveMessage(message);
    }


    @OnError
    public void onError(Session session, Throwable error) {
        LogServer.getInstance().removeClient(this);
        this.mSession = null;
    }

}
