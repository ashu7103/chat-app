# 💬 Real-Time Chat Application

A responsive real-time chat application built with **Spring Boot**, **WebSocket (STOMP/SockJS)**, **MySQL**, and **Tailwind CSS**.  
It allows users to register, log in, join multiple rooms, and exchange messages live. Ideal for collaborative environments and modern web communication.

---
## ✨ Features
- 🔐 User authentication (Register/Login)
- 💬 Real-time messaging with WebSocket (STOMP over SockJS)
- 🧑‍🤝‍🧑 Active user display in each chat room
- 🖊️ Typing indicators when a user is typing
- 🏷️ Create and join multiple chat rooms
- 📜 Auto-load recent chat history on room join
- 📢 Notifications for messages from other rooms
- 🧠 Clean and responsive Tailwind UI
---
## 🔧 Tech Stack
| Layer      | Technology                     |
| ---------- | ------------------------------ |
| Backend    | Spring Boot, Spring Security   |
| Real-Time  | WebSocket (STOMP + SockJS)     |
| Frontend   | HTML, Tailwind CSS, Vanilla JS |
| Database   | MySQL                          |
| Build Tool | Maven                          |

---
## 📸 Screenshots

- **Login & Register UI**  
<img width="728" height="975" alt="image" src="https://github.com/user-attachments/assets/6afdcac2-8e0b-43b9-b8be-dbcbb7d179f3" />


- **Chat Interface with Messages**  
<img width="587" height="821" alt="image" src="https://github.com/user-attachments/assets/0fb4b765-007a-488e-b52d-079945149a9f" />
---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/yourusername/chat-app.git
Open folder using Intellij
````

### 2. Set up MySQL Database

Create a database in MySQL:

```sql
CREATE DATABASE chatdb;
```

Update `application.properties` (or `application.yml`) with your DB credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/chatdb
spring.datasource.username=root
spring.datasource.password=your_password
```

---

### 3. Run the Application

Make sure you're using Java 17+ and Maven installed.

```bash
./mvnw spring-boot:run
```

Or run via IntelliJ: `ChatAppApplication.java`

---

### 4. Access the Application

Frontend will be available at:

```
http://localhost:8080
```

Register or Login and start chatting!

---

## 📂 Project Structure

```bash
├── src/main/java/com/example/chat
│   ├── controller
│   ├── model
│   ├── repository
│   ├── service
│   ├── websocket
│   └── ChatAppApplication.java
├── src/main/resources
│   ├── static (HTML, JS)
│   ├── templates
│   └── application.properties
├── screenshots/
├── .gitignore
└── README.md
```

---

## 💡 Future Enhancements

* ✅ One-on-one private messaging
* ✅ Message delete/edit
* ✅ Web push notifications
* ✅ Emoji and file sharing
* ✅ Dockerize and deploy to cloud

---

## 🙌 Contribution

Feel free to fork, submit issues or PRs. Let’s build more together!

---

## 📄 License

MIT License - [LICENSE](LICENSE)

```
