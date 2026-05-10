package com.astha.performance_dashboard.websocket;

import com.astha.performance_dashboard.dto.WebSocketMessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler implements org.springframework.web.reactive.socket.WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Sinks.Many<WebSocketMessageDTO> messageSink = Sinks.many().multicast().onBackpressureBuffer();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("WebSocket connected: {} (Total connections: {})", sessionId, sessions.size());

        // Send welcome message
        WebSocketMessageDTO welcome = WebSocketMessageDTO.builder()
            .type("CONNECTED")
            .assetId(sessionId)
            .payload(Map.of("sessionId", sessionId, "message", "Connected to performance monitoring"))
            .timestamp(LocalDateTime.now())
            .build();

        // Heartbeat to keep connection alive
        Flux<WebSocketMessageDTO> heartbeat = Flux.interval(Duration.ofSeconds(30))
            .map(tick -> WebSocketMessageDTO.builder()
                .type("HEARTBEAT")
                .assetId(sessionId)
                .payload(Map.of("tick", tick))
                .timestamp(LocalDateTime.now())
                .build());

        // Combine welcome, broadcast messages, and heartbeat
        Flux<WebSocketMessageDTO> messageFlux = Flux.concat(
            Flux.just(welcome),
            messageSink.asFlux(),
            heartbeat
        );

        // Send messages to client
        Mono<Void> output = session.send(
            messageFlux
                .map(msg -> {
                    try {
                        return objectMapper.writeValueAsString(msg);
                    } catch (Exception e) {
                        log.error("Failed to serialize message", e);
                        return "{}";
                    }
                })
                .map(session::textMessage)
        );

        // Handle incoming messages from client
        Mono<Void> input = session.receive()
            .map(webSocketMessage -> {
                try {
                    return objectMapper.readValue(
                        webSocketMessage.getPayloadAsText(),
                        WebSocketMessageDTO.class
                    );
                } catch (Exception e) {
                    log.error("Failed to parse incoming message", e);
                    return null;
                }
            })
            .filter(msg -> msg != null)
            .doOnNext(msg -> {
                log.debug("Received message from {}: {}", sessionId, msg.getType());
                handleIncomingMessage(sessionId, msg);
            })
            .then();

        // Cleanup on disconnect
        return Mono.zip(input, output)
            .doFinally(signalType -> {
                sessions.remove(sessionId);
                log.info("WebSocket disconnected: {} (Total connections: {})", sessionId, sessions.size());
            })
            .then();
    }

    private void handleIncomingMessage(String sessionId, WebSocketMessageDTO message) {
        // Handle different message types from clients
        switch (message.getType()) {
            case "SUBSCRIBE" -> log.info("Session {} subscribed to asset: {}", sessionId, message.getAssetId());
            case "UNSUBSCRIBE" -> log.info("Session {} unsubscribed from asset: {}", sessionId, message.getAssetId());
            case "PING" -> {
                WebSocketMessageDTO pong = WebSocketMessageDTO.builder()
                    .type("PONG")
                    .assetId(message.getAssetId())
                    .timestamp(LocalDateTime.now())
                    .build();
                sendToSession(sessionId, pong);
            }
            default -> log.warn("Unknown message type: {}", message.getType());
        }
    }

    /**
     * Broadcast message to all connected clients
     */
    public void broadcast(WebSocketMessageDTO message) {
        messageSink.tryEmitNext(message);
        log.debug("Broadcasting message to {} sessions: {}", sessions.size(), message.getType());
    }

    /**
     * Send message to specific session
     */
    public void sendToSession(String sessionId, WebSocketMessageDTO message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.send(Mono.just(session.textMessage(json))).subscribe();
            } catch (Exception e) {
                log.error("Failed to send message to session {}", sessionId, e);
            }
        }
    }

    /**
     * Send message to sessions subscribed to specific asset
     */
    public void broadcastToAsset(String assetId, WebSocketMessageDTO message) {
        message.setAssetId(assetId);
        broadcast(message);
    }

    /**
     * Get active connection count
     */
    public int getConnectionCount() {
        return sessions.size();
    }

    /**
     * Disconnect all sessions
     */
    public void disconnectAll() {
        sessions.values().forEach(session -> {
            try {
                session.close().subscribe();
            } catch (Exception e) {
                log.error("Error closing session", e);
            }
        });
        sessions.clear();
    }
}
