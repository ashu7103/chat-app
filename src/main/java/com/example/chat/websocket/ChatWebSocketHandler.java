package com.example.chat.websocket;

import com.example.chat.exception.CustomException;
import com.example.chat.model.Message;
import com.example.chat.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final Map<String, List<String>> roomUsers = new ConcurrentHashMap<>();
    @Autowired
    private ChatService chatService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MessageMapping("/room/{roomId}")
    public void handleMessage(@DestinationVariable String roomId, @Payload String payload) throws Exception {
        try {
            logger.debug("Received WebSocket message for room {}: {}", roomId, payload);
            Map<String, Object> json = objectMapper.readValue(payload, Map.class);
            String type = (String) json.get("type");

            if ("message".equals(type)) {
                Message msg = objectMapper.convertValue(json, Message.class);
                logger.info("Processing message for room {} from user {}: {}", msg.getRoomId(), msg.getUserId(), msg.getMessageText());
                msg = chatService.saveMessage(msg);
                messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                        "type", "message",
                        "data", msg
                ));
                broadcastNotification(msg);
                logger.info("Message broadcasted to room {} by user {}", msg.getRoomId(), msg.getUserId());
            } else if ("typing".equals(type)) {
                String username = (String) json.get("username");
                messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                        "type", "typing",
                        "data", Map.of("username", username)
                ));
                logger.debug("Typing event from {} in room {}", username, roomId);
            } else if ("join".equals(type)) {
                String username = (String) json.get("username");
                roomUsers.computeIfAbsent(roomId, k -> new ArrayList<>()).add(username);
                logger.info("User {} joined room {}", username, roomId);
                broadcastUserList(roomId);
            } else if ("leave".equals(type)) {
                String username = (String) json.get("username");
                roomUsers.getOrDefault(roomId, new ArrayList<>()).remove(username);
                logger.info("User {} left room {}", username, roomId);
                broadcastUserList(roomId);
            } else {
                logger.warn("Unknown message type: {}", type);
                throw new CustomException("Invalid message type", 400);
            }
        } catch (CustomException e) {
            logger.warn("WebSocket error: {}", e.getMessage());
            messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                    "type", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error handling WebSocket message for room {}: {}", roomId, e.getMessage(), e);
            messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                    "type", "error",
                    "message", "Server error"
            ));
        }
    }

    private void broadcastUserList(String roomId) {
        List<String> usernames = roomUsers.getOrDefault(roomId, new ArrayList<>());
        messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                "type", "userList",
                "data", usernames
        ));
        logger.debug("Broadcasted user list for room {}: {}", roomId, usernames);
    }

    private void broadcastNotification(Message message) throws Exception {
        try {
            String roomName = chatService.getRoomNameById(message.getRoomId());
            String preview = message.getMessageText().length() > 20
                    ? message.getMessageText().substring(0, 20) + "..."
                    : message.getMessageText();
            String jsonNotification = objectMapper.writeValueAsString(Map.of(
                    "type", "notification",
                    "data", Map.of(
                            "roomId", message.getRoomId(),
                            "roomName", roomName,
                            "messageText", preview
                    )
            ));
            for (String otherRoomId : roomUsers.keySet()) {
                if (!otherRoomId.equals(String.valueOf(message.getRoomId()))) {
                    messagingTemplate.convertAndSend("/topic/room/" + otherRoomId, jsonNotification);
                }
            }
            logger.debug("Notification sent for message in room {}: {}", message.getRoomId(), preview);
        } catch (Exception e) {
            logger.error("Error broadcasting notification for room {}: {}", message.getRoomId(), e.getMessage(), e);
            throw e;
        }
    }
}