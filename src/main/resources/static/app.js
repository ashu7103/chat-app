// Store logged-in user, current room, and WebSocket connection
let currentUser = null;
let currentRoomId = null;
let ws = null;
let typingTimeout = null;

// DOM elements
const loginSection = document.getElementById('login-section');
const chatSection = document.getElementById('chat-section');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const registerForm = document.getElementById('register-form');
const registerError = document.getElementById('register-error');
const roomSelect = document.getElementById('room-select');
const createRoomForm = document.getElementById('create-room-form');
const createRoomError = document.getElementById('create-room-error');
const userList = document.getElementById('user-list');
const messagesDiv = document.getElementById('messages');
const messageForm = document.getElementById('message-form');
const messageInput = document.getElementById('message-input');
const typingIndicator = document.getElementById('typing-indicator');
const notificationArea = document.getElementById('notification-area');

// Handle registration
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('register-username').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;
    try {
        const response = await fetch('http://localhost:8080/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });
        if (!response.ok) throw new Error('Registration failed');
        currentUser = await response.json();
        loginSection.classList.add('hidden');
        chatSection.classList.remove('hidden');
        loadRooms();
    } catch (error) {
        registerError.classList.remove('hidden');
        registerError.textContent = error.message;
        console.error('Registration error:', error);
    }
});

// Handle login
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        if (!response.ok) throw new Error('Invalid credentials');
        currentUser = await response.json();
        loginSection.classList.add('hidden');
        chatSection.classList.remove('hidden');
        loadRooms();
    } catch (error) {
        loginError.classList.remove('hidden');
        loginError.textContent = error.message;
        console.error('Login error:', error);
    }
});

// Load chat rooms
async function loadRooms() {
    try {
        const response = await fetch('http://localhost:8080/api/rooms');
        if (!response.ok) throw new Error('Failed to fetch rooms');
        const rooms = await response.json();
        roomSelect.innerHTML = '<option value="">Select a room</option>';
        rooms.forEach(room => {
            const option = document.createElement('option');
            option.value = room.id;
            option.textContent = room.name;
            roomSelect.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading rooms:', error);
        alert('Failed to load rooms');
    }
}

// Handle create room
createRoomForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const roomName = document.getElementById('room-name').value;
    try {
        const response = await fetch('http://localhost:8080/api/rooms', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: roomName })
        });
        if (!response.ok) throw new Error('Failed to create room');
        document.getElementById('room-name').value = '';
        createRoomError.classList.add('hidden');
        await loadRooms();
    } catch (error) {
        createRoomError.classList.remove('hidden');
        createRoomError.textContent = error.message;
        console.error('Error creating room:', error);
    }
});

// Connect to WebSocket and load message history when selecting a room
roomSelect.addEventListener('change', async (e) => {
    currentRoomId = e.target.value;
    if (currentRoomId) {
        try {
            if (ws) {
                ws.disconnect();
            }
            const sock = new SockJS('/chat');
            ws = Stomp.over(sock);
            ws.connect({}, (frame) => {
                console.log(`Connected to WebSocket for room ${currentRoomId}`);
//                console.log('✅ STOMP CONNECTED', frame);
                ws.send(`/app/room/${currentRoomId}`, {}, JSON.stringify({
                    type: 'join',
                    username: currentUser.username
                }));
                ws.subscribe(`/topic/room/${currentRoomId}`, (message) => {
                    try {
                        const data = JSON.parse(message.body);
                        console.log('Received WebSocket message:', data);
                        if (data.type === 'message') {
                            appendMessage(data.data);
                        } else if (data.type === 'userList') {
                            updateUserList(data.data);
                        } else if (data.type === 'typing') {
                            showTypingIndicator(data.data.username);
                        } else if (data.type === 'notification') {
                            showNotification(data.data);
                        } else if (data.type === 'error') {
                            console.error('WebSocket error:', data.message);
                            alert(`Chat error: ${data.message}`);
                        }
                    } catch (error) {
                        console.error('Error parsing WebSocket message:', error);
                    }
                });
            }, (error) => {
                console.error('WebSocket connection error:', error);
                alert('Failed to connect to chat room');
            });

            messagesDiv.innerHTML = '';
            const response = await fetch(`http://localhost:8080/api/rooms/${currentRoomId}/messages`);
            if (!response.ok) throw new Error('Failed to fetch messages');
            const messages = await response.json();
            console.log('Fetched messages:', messages);
            messages.forEach(message => appendMessage(message));
        } catch (error) {
            console.error('Error setting up room:', error);
            alert('Failed to join room');
        }
    }
});

// Send message via WebSocket
messageForm.addEventListener('submit', (e) => {
    e.preventDefault();
    if (ws && currentRoomId && currentUser && messageInput.value) {
        try {
            const message = {
                type: 'message',
                roomId: parseInt(currentRoomId),
                userId: currentUser.id,
//               // userName: currentUser.username,
                //-----------------------changes here
                messageText: messageInput.value
            };
            ws.send(`/app/room/${currentRoomId}`, {}, JSON.stringify(message));
            console.log('Sent message:', message);
            messageInput.value = '';
        } catch (error) {
            console.error('Error sending message:', error);
            alert('Failed to send message');
        }
    }
});

// Send typing event
messageInput.addEventListener('input', () => {
    if (ws && currentRoomId && currentUser) {
        try {
            const typingMessage = {
                type: 'typing',
                username: currentUser.username,
                roomId: parseInt(currentRoomId)
            };
            ws.send(`/app/room/${currentRoomId}`, {}, JSON.stringify(typingMessage));
            console.log('Sent typing event:', typingMessage);
        } catch (error) {
            console.error('Error sending typing event:', error);
        }
    }
});

// Handle window unload to send leave message
window.addEventListener('beforeunload', () => {
    if (ws && currentRoomId && currentUser) {
        ws.send(`/app/room/${currentRoomId}`, {}, JSON.stringify({
            type: 'leave',
            username: currentUser.username
        }));
        ws.disconnect();
    }
});
//--------------fetching username----------------
async function getUsernameById(userId) {
    try {
        const response = await fetch(`http://localhost:8080/api/rooms/${userId}`);
        if (!response.ok) throw new Error(`Failed to fetch username for ID ${userId}`);
        const data = await response.json(); // Parse JSON
        return data.username; // ✅ Return only the username string
    } catch (error) {
        console.error('Error fetching username:', error);
        return `UnknownUser(${userId})`;
    }
}

// Append a message to the UI
async function appendMessage(message) {
    const div = document.createElement('div');
//    div.className = 'p-2 border-b';
//    //---------------------------------id to name of user--------------------------
//    div.textContent = `[${new Date(message.timestamp).toLocaleTimeString()}]  ${message.userId}: ${message.messageText}`;
//    messagesDiv.appendChild(div);
    div.className = 'flex flex-col items-start';
    const username = await getUsernameById(message.userId);

    div.innerHTML = `
    <div class="bg-blue-100 px-4 py-2 rounded-lg shadow-sm max-w-[75%]">
      <p class="font-semibold text-blue-700">${username}</p>
      <p class="text-sm">${message.messageText}</p>
    </div>
    <span class="text-xs text-gray-400 mt-1 ml-2">${new Date(message.timestamp).toLocaleTimeString()}</span>
    `;
    messagesDiv.appendChild(div);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
    console.log('Appended message:', message);
}

// Update user list in the UI
function updateUserList(usernames) {
    userList.innerHTML = usernames.length ? usernames.join(', ') : 'No users online';
}

// Show typing indicator with timeout
function showTypingIndicator(username) {
    if (username !== currentUser.username) {
        typingIndicator.innerHTML = `${username} is typing...`;
        clearTimeout(typingTimeout);
        typingTimeout = setTimeout(() => {
            typingIndicator.innerHTML = '';
        }, 3000);
    }
}

// Show notification for messages in other rooms
function showNotification(data) {
    const div = document.createElement('div');
    div.className = 'bg-yellow-100 p-2 mb-2 border rounded';
    div.textContent = `New message in ${data.roomName}: ${data.messageText}`;
    notificationArea.appendChild(div);
    alert(`New message in ${data.roomName}: ${data.messageText}`);
    setTimeout(() => {
        div.remove();
    }, 5000);
}