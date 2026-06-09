package com.sky.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {

    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        log.info("client connected: {}", sid);
        SESSION_MAP.put(sid, session);
    }


    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        log.info("client disconnected: {}", sid);
        SESSION_MAP.remove(sid);
    }

    public void sendToAllClient(String message) {
        Collection<Session> sessions = SESSION_MAP.values();
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.error("websocket send message failed", e);
            }
        }
    }

}
