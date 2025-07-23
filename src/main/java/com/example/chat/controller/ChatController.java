package com.example.chat.controller;

import com.example.chat.exception.CustomException;
import com.example.chat.model.ChatRoom;
import com.example.chat.model.Message;
import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import com.example.chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, String>> getUsernameById(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(Map.of("username", user.getUsername())))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found")));
    }


    @GetMapping
    public ResponseEntity<List<ChatRoom>> getAllRooms() {
        try {
            List<ChatRoom> rooms = chatService.getAllRooms();
            logger.info("Fetched {} chat rooms", rooms.size());
            return ResponseEntity.ok(rooms);
        } catch (CustomException e) {
            logger.warn("Error fetching rooms: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error fetching rooms: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<Message>> getMessagesByRoomId(@PathVariable Long roomId) {
        try {
            List<Message> messages = chatService.getMessagesByRoomId(roomId);
            logger.info("Fetched {} messages for room {}", messages.size(), roomId);
            return ResponseEntity.ok(messages);
        } catch (CustomException e) {
            logger.warn("Error fetching messages for room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error fetching messages for room {}: {}", roomId, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    // New endpoint for creating a chat room
    @PostMapping
    public ResponseEntity<ChatRoom> createRoom(@RequestBody ChatRoom room) {
        try {
            ChatRoom createdRoom = chatService.createRoom(room.getName());
            logger.info("Created chat room: {}", createdRoom.getName());
            return ResponseEntity.ok(createdRoom);
        } catch (CustomException e) {
            logger.warn("Error creating room: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error creating room: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}
