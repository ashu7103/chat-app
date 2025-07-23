-- Initialize chat rooms
INSERT INTO chat_rooms (name) VALUES ('General');
INSERT INTO chat_rooms (name) VALUES ('Tech');

-- Initialize sample messages for testing (assumes user ID 1 exists)
INSERT INTO messages (room_id, user_id, message_text, timestamp) VALUES (1, 1, 'Welcome to General!', NOW());
INSERT INTO messages (room_id, user_id, message_text, timestamp) VALUES (1, 1, 'Test message in General', NOW());