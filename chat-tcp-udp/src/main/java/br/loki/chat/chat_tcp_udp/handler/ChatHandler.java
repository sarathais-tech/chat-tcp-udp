package br.loki.chat.chat_tcp_udp.handler;

import br.loki.chat.chat_tcp_udp.ai.LokiAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final LokiAiService ai;

    // Estado (thread-safe)
    private final Map<WebSocketSession, String> sessionToUser = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> userToSession = new ConcurrentHashMap<>();

    public ChatHandler(LokiAiService ai) {
        this.ai = ai;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("TCP: Cliente conectado -> " + session.getRemoteAddress());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> msg = mapper.readValue(message.getPayload(), Map.class);
        String type = asString(msg.get("type"));

        switch (type) {
            case "JOIN" -> handleJoin(session, asString(msg.get("user")));
            case "GLOBAL_MSG" -> handleGlobal(session, asString(msg.get("text")));
            case "DM" -> handleDm(session, asString(msg.get("to")), asString(msg.get("text")));
            case "BOT" -> handleBot(session, asString(msg.get("mode")), asString(msg.get("text")));
            default -> sendError(session, "Tipo inválido: " + type);
        }
    }

    private void handleJoin(WebSocketSession session, String user) throws Exception {
        if (user == null || user.isBlank()) {
            sendError(session, "Nome inválido.");
            return;
        }
        if (userToSession.containsKey(user)) {
            sendError(session, "Nome já em uso.");
            return;
        }

        sessionToUser.put(session, user);
        userToSession.put(user, session);

        System.out.println("TCP: JOIN -> " + user);

        // avisa todos
        broadcastUsers();
        broadcastMsg("GLOBAL", "System", null, user + " entrou no chat.");
    }

    private void handleGlobal(WebSocketSession session, String text) throws Exception {
        String from = sessionToUser.get(session);
        if (from == null) { sendError(session, "Faça JOIN primeiro."); return; }

        broadcastMsg("GLOBAL", from, null, text);
    }

    private void handleDm(WebSocketSession session, String to, String text) throws Exception {
        String from = sessionToUser.get(session);
        if (from == null) { sendError(session, "Faça JOIN primeiro."); return; }
        if (to == null || to.isBlank()) { sendError(session, "Destino inválido."); return; }

        WebSocketSession toSession = userToSession.get(to);
        if (toSession == null || !toSession.isOpen()) {
            sendError(session, "Usuário '" + to + "' não está online.");
            return;
        }

        // envia para os dois (pra aparecer no chat do remetente e do destinatário)
        sendMsg(session, "DM", from, to, text);
        sendMsg(toSession, "DM", from, to, text);
    }

    private void handleBot(WebSocketSession session, String mode, String text) throws Exception {
        String from = sessionToUser.get(session);
        if (from == null) { sendError(session, "Faça JOIN primeiro."); return; }

        String botMode = (mode == null) ? "PRIVATE" : mode.toUpperCase();
        String prompt = (text == null) ? "" : text;

        // roda em thread pra não travar a conexão
        new Thread(() -> {
            try {
                String answer = ai.ask(from, prompt);
                if ("GLOBAL".equals(botMode)) {
                    broadcastMsg("BOT", "LokiBot", null, answer);
                } else {
                    // PRIVATE
                    WebSocketSession fromSession = userToSession.get(from);
                    if (fromSession != null && fromSession.isOpen()) {
                        sendMsg(fromSession, "BOT_PRIVATE", "LokiBot", from, answer);
                    }
                }
            } catch (Exception ignored) {}
        }, "loki-ai").start();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String user = sessionToUser.remove(session);
        if (user != null) {
            userToSession.remove(user);
            System.out.println("TCP: Cliente desconectado -> " + user);
            try {
                broadcastUsers();
                broadcastMsg("GLOBAL", "System", null, user + " saiu do chat.");
            } catch (Exception ignored) {}
        }
    }

    // ===== helpers =====

    private void broadcastUsers() throws Exception {
        List<String> users = new ArrayList<>(userToSession.keySet());
        users.sort(String::compareToIgnoreCase);

        Map<String, Object> payload = Map.of("type", "USERS", "users", users);
        broadcastJson(payload);
    }

    private void broadcastMsg(String scope, String from, String to, String text) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "MSG");
        payload.put("scope", scope);
        payload.put("from", from);
        if (to != null) payload.put("to", to);
        payload.put("text", text);
        broadcastJson(payload);
    }

    private void sendMsg(WebSocketSession session, String scope, String from, String to, String text) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "MSG");
        payload.put("scope", scope);
        payload.put("from", from);
        if (to != null) payload.put("to", to);
        payload.put("text", text);
        session.sendMessage(new TextMessage(mapper.writeValueAsString(payload)));
    }

    private void broadcastJson(Map<String, Object> payload) throws Exception {
        String json = mapper.writeValueAsString(payload);
        for (WebSocketSession s : sessionToUser.keySet()) {
            if (s.isOpen()) s.sendMessage(new TextMessage(json));
        }
        // também inclui quem conectou mas ainda não deu JOIN? opcional
        // aqui eu só mando para quem já deu JOIN (sessionToUser.keySet())
    }

    private void sendError(WebSocketSession session, String err) throws Exception {
        Map<String, Object> payload = Map.of("type", "ERROR", "message", err);
        session.sendMessage(new TextMessage(mapper.writeValueAsString(payload)));
    }

    private String asString(Object o) { return o == null ? null : String.valueOf(o); }
}