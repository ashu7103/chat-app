package com.example.chat.service;

import com.example.chat.exception.CustomException;
import com.example.chat.model.ChatRoom;
import com.example.chat.model.Message;
import com.example.chat.repository.ChatRoomRepository;
import com.example.chat.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private MessageRepository messageRepository;

    public List<ChatRoom> getAllRooms() {
        try {
            return chatRoomRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching chat rooms: {}", e.getMessage(), e);
            throw new CustomException("Failed to fetch chat rooms", 500);
        }
    }

    public List<Message> getMessagesByRoomId(Long roomId) {
        try {
            return messageRepository.findByRoomIdOrderByTimestampAsc(roomId);
        } catch (Exception e) {
            logger.error("Error fetching messages for room {}: {}", roomId, e.getMessage(), e);
            throw new CustomException("Failed to fetch messages for room " + roomId, 500);
        }
    }

    public Message saveMessage(Message message) {
        try {
            if (message.getRoomId() == null || message.getUserId() == null || message.getMessageText() == null) {
                logger.warn("Invalid message data: roomId={}, userId={}, messageText={}",
                        message.getRoomId(), message.getUserId(), message.getMessageText());
                throw new CustomException("Invalid message data", 400);
            }
            message.setTimestamp(LocalDateTime.now());
            Message savedMessage = messageRepository.save(message);
            logger.info("Message saved for room {} by user {}", message.getRoomId(), message.getUserId());
            return savedMessage;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error saving message: {}", e.getMessage(), e);
            throw new CustomException("Failed to save message", 500);
        }
    }

    public String getRoomNameById(Long roomId) {
        try {
            return chatRoomRepository.findById(roomId)
                    .map(ChatRoom::getName)
                    .orElseThrow(() -> new CustomException("Room not found", 404));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching room name for room {}: {}", roomId, e.getMessage(), e);
            throw new CustomException("Failed to fetch room name", 500);
        }
    }

    // New method for creating a chat room
    public ChatRoom createRoom(String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                logger.warn("Invalid room name: {}", name);
                throw new CustomException("Room name cannot be empty", 400);
            }
            ChatRoom room = new ChatRoom();
            room.setName(name.trim());
            ChatRoom savedRoom = chatRoomRepository.save(room);
            logger.info("Chat room created: {}", name);
            return savedRoom;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating chat room {}: {}", name, e.getMessage(), e);
            throw new CustomException("Failed to create chat room", 500);
        }
    }
}
