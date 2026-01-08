# Giải Thích Chi Tiết Luồng Xử Lý Request, Response và Socket

## 📋 Mục Lục
1. [Luồng Request → Object (HTTP REST API)](#1-luồng-request--object-http-rest-api)
2. [Luồng Object → Response](#2-luồng-object--response)
3. [Luồng Socket (WebSocket)](#3-luồng-socket-websocket)
4. [Ví Dụ Cụ Thể: Gửi Tin Nhắn Chat](#4-ví-dụ-cụ-thể-gửi-tin-nhắn-chat)

---

## 1. Luồng Request → Object (HTTP REST API)

### 1.1. Tổng Quan
Khi client (web-app) gửi HTTP request đến server (chat-service hoặc identity-service), Spring Boot tự động chuyển đổi JSON trong request body thành Java Object.

### 1.2. Các Bước Chi Tiết

#### **Bước 1: Client Gửi Request (Frontend)**
```javascript
// web-app/src/services/chatService.js
export const createMessage = async (data) => {
  return await httpClient.post(
    API.CREATE_MESSAGE,
    {
      conversationId: data.conversationId,  // JavaScript Object
      message: data.message,
    },
    {
      headers: {
        Authorization: `Bearer ${getToken()}`,
        "Content-Type": "application/json",  // Báo cho server biết là JSON
      },
    }
  );
};
```

**Điều gì xảy ra:**
- JavaScript object `{conversationId: "...", message: "..."}` được tự động chuyển thành JSON string
- Axios gửi HTTP POST request với body là JSON string

#### **Bước 2: Spring Boot Nhận Request (Backend)**

```java
// chat-service/src/main/java/com/devteria/chat/controller/ChatMessageController.java
@PostMapping("/create")
ApiResponse<ChatMessageResponse> create(
        @RequestBody @Valid ChatMessageRequest request) throws JsonProcessingException {
    return ApiResponse.<ChatMessageResponse>builder()
            .result(chatMessageService.create(request))
            .build();
}
```

**Giải thích:**
- `@PostMapping("/create")`: Định nghĩa endpoint nhận POST request
- `@RequestBody`: **Đây là annotation quan trọng!** Nó báo cho Spring Boot biết:
  - Lấy dữ liệu từ HTTP request body
  - Tự động chuyển đổi JSON string thành Java Object
  - Sử dụng Jackson library (có sẵn trong Spring Boot) để parse JSON
- `@Valid`: Kiểm tra validation (ví dụ: `@NotBlank` trong DTO)

#### **Bước 3: Spring Boot Tự Động Parse JSON → Java Object**

Spring Boot sử dụng **Jackson ObjectMapper** để chuyển đổi:

```json
// JSON từ request body
{
  "conversationId": "123",
  "message": "Hello world"
}
```

↓ **Jackson tự động map** ↓

```java
// Java Object (ChatMessageRequest)
ChatMessageRequest request = new ChatMessageRequest();
request.setConversationId("123");
request.setMessage("Hello world");
```

**Cách hoạt động:**
1. Jackson đọc JSON string từ HTTP body
2. Tìm class `ChatMessageRequest` (từ `@RequestBody`)
3. So sánh tên field trong JSON với tên field trong Java class
4. Tạo instance của `ChatMessageRequest` và set giá trị

#### **Bước 4: DTO Class Định Nghĩa Cấu Trúc**

```java
// chat-service/src/main/java/com/devteria/chat/dto/request/ChatMessageRequest.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageRequest {
    @NotBlank  // Validation: không được để trống
    String conversationId;

    @NotBlank
    String message;
}
```

**Lưu ý:**
- Tên field trong JSON **phải khớp** với tên field trong Java class (case-sensitive)
- Jackson tự động convert kiểu dữ liệu (String → String, Number → int/long, etc.)

---

## 2. Luồng Object → Response

### 2.1. Tổng Quan
Sau khi xử lý business logic, server cần chuyển đổi Java Object thành JSON response để gửi về client.

### 2.2. Các Bước Chi Tiết

#### **Bước 1: Service Xử Lý và Tạo Response Object**

```java
// chat-service/src/main/java/com/devteria/chat/service/ChatMessageService.java
public ChatMessageResponse create(ChatMessageRequest request) throws JsonProcessingException {
    // ... xử lý business logic ...
    
    // Tạo Entity từ Request
    ChatMessage chatMessage = chatMessageMapper.toChatMessage(request);
    chatMessage.setSender(ParticipantInfo.builder()
            .userId(userInfo.getUserId())
            .username(userInfo.getUsername())
            // ...
            .build());
    chatMessage.setCreatedDate(Instant.now());
    
    // Lưu vào database
    chatMessage = chatMessageRepository.save(chatMessage);
    
    // Chuyển Entity → Response DTO
    return toChatMessageResponse(chatMessage);
}
```

#### **Bước 2: Mapper Chuyển Đổi Entity → Response DTO**

```java
// chat-service/src/main/java/com/devteria/chat/mapper/ChatMessageMapper.java
@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);
    ChatMessage toChatMessage(ChatMessageRequest request);
}
```

**MapStruct tự động generate code:**
```java
// Code được generate tự động (bạn không cần viết)
@Override
public ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage) {
    ChatMessageResponse response = new ChatMessageResponse();
    response.setId(chatMessage.getId());
    response.setConversationId(chatMessage.getConversationId());
    response.setMessage(chatMessage.getMessage());
    response.setSender(chatMessage.getSender());
    response.setCreatedDate(chatMessage.getCreatedDate());
    return response;
}
```

#### **Bước 3: Controller Wrap Response trong ApiResponse**

```java
// chat-service/src/main/java/com/devteria/chat/controller/ChatMessageController.java
@PostMapping("/create")
ApiResponse<ChatMessageResponse> create(
        @RequestBody @Valid ChatMessageRequest request) {
    return ApiResponse.<ChatMessageResponse>builder()
            .result(chatMessageService.create(request))  // ChatMessageResponse object
            .build();
}
```

**Cấu trúc ApiResponse:**
```java
// chat-service/src/main/java/com/devteria/chat/dto/ApiResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @Builder.Default
    private int code = 1000;  // Mã lỗi/thành công
    private String message;   // Thông báo
    private T result;         // Dữ liệu thực tế (generic type)
}
```

#### **Bước 4: Spring Boot Tự Động Serialize Object → JSON**

Spring Boot sử dụng **Jackson** để chuyển đổi:

```java
// Java Object
ApiResponse<ChatMessageResponse> response = ApiResponse.builder()
    .code(1000)
    .result(ChatMessageResponse.builder()
        .id("msg123")
        .conversationId("conv456")
        .message("Hello")
        .build())
    .build();
```

↓ **Jackson tự động serialize** ↓

```json
// JSON response
{
  "code": 1000,
  "message": null,
  "result": {
    "id": "msg123",
    "conversationId": "conv456",
    "message": "Hello",
    "me": false,
    "sender": {
      "userId": "user789",
      "username": "john_doe"
    },
    "createdDate": "2024-01-15T10:30:00Z"
  }
}
```

**Cách hoạt động:**
1. Controller method return `ApiResponse<ChatMessageResponse>`
2. Spring Boot tự động gọi Jackson để serialize
3. Jackson đọc tất cả getter methods (hoặc fields nếu public)
4. Tạo JSON string
5. Gửi về client với `Content-Type: application/json`

#### **Bước 5: Client Nhận và Parse JSON**

```javascript
// web-app/src/pages/Chat.jsx
const handleSendMessage = async () => {
  try {
    const response = await createMessage({
      conversationId: selectedConversation.id,
      message: message,
    });
    // response.data đã là JavaScript object (Axios tự động parse JSON)
    console.log(response.data.result);  // ChatMessageResponse object
  } catch (error) {
    console.error("Failed to send message:", error);
  }
};
```

**Điều gì xảy ra:**
- Axios nhận JSON string từ server
- Tự động parse thành JavaScript object
- `response.data` là object, không phải JSON string

---

## 3. Luồng Socket (WebSocket)

### 3.1. Tổng Quan
Socket.IO cho phép giao tiếp **real-time** giữa client và server, không cần HTTP request/response mỗi lần.

### 3.2. Kiến Trúc Socket.IO

```
Client (React)                    Server (Spring Boot)
    |                                    |
    |---- connect (với token) -------->|
    |                                    | (Xác thực token)
    |<---- connected -------------------|
    |                                    |
    |                                    | (Khi có tin nhắn mới)
    |<---- "message" event -------------|
    |                                    |
    |---- disconnect ------------------>|
```

### 3.3. Các Bước Chi Tiết

#### **Bước 1: Server Khởi Tạo Socket Server**

```java
// chat-service/src/main/java/com/devteria/chat/configuration/SocketIOConfig.java
@Configuration
public class SocketIOConfig {
    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration configuration = 
            new com.corundumstudio.socketio.Configuration();
        configuration.setPort(8099);  // Port riêng cho socket
        configuration.setOrigin("*");  // Cho phép tất cả origin
        
        return new SocketIOServer(configuration);
    }
}
```

#### **Bước 2: Client Kết Nối Socket**

```javascript
// web-app/src/pages/Chat.jsx
useEffect(() => {
  if (!socketRef.current) {
    // Tạo kết nối socket với token trong query string
    const connectionUrl = "http://localhost:8099?token=" + getToken();
    
    socketRef.current = new io(connectionUrl);  // socket.io-client
    
    // Lắng nghe sự kiện "connect"
    socketRef.current.on("connect", () => {
      console.log("Socket connected");
    });
    
    // Lắng nghe sự kiện "disconnect"
    socketRef.current.on("disconnect", () => {
      console.log("Socket disconnected");
    });
    
    // Lắng nghe sự kiện "message" (tin nhắn mới)
    socketRef.current.on("message", (message) => {
      console.log("New message received:", message);
      const messageObject = JSON.parse(message);  // Parse JSON string
      handleIncomingMessage(messageObject);
    });
  }
}, []);
```

**Điều gì xảy ra:**
1. Client tạo WebSocket connection đến `http://localhost:8099`
2. Gửi token trong query string: `?token=abc123...`
3. Server nhận connection request

#### **Bước 3: Server Xác Thực Khi Client Connect**

```java
// chat-service/src/main/java/com/devteria/chat/controller/SocketHandler.java
@OnConnect
public void clientConnected(SocketIOClient client) {
    // Lấy token từ query parameter
    String token = client.getHandshakeData().getSingleUrlParam("token");
    
    // Xác thực token với Identity Service
    var introspectResponse = identityService.introspect(
        IntrospectRequest.builder()
            .token(token)
            .build()
    );
    
    // Nếu token hợp lệ
    if (introspectResponse.isValid()) {
        log.info("Client connected: {}", client.getSessionId());
        
        // Lưu WebSocket session vào database
        WebSocketSession webSocketSession = WebSocketSession.builder()
                .socketSessionId(client.getSessionId().toString())
                .userId(introspectResponse.getUserId())
                .createdAt(Instant.now())
                .build();
        webSocketSession = webSocketSessionService.create(webSocketSession);
        
    } else {
        // Token không hợp lệ → disconnect
        log.error("Authentication fail: {}", client.getSessionId());
        client.disconnect();
    }
}
```

**Lưu ý quan trọng:**
- Mỗi client có một `sessionId` duy nhất
- Server lưu mapping: `sessionId` ↔ `userId`
- Nếu token không hợp lệ, server disconnect ngay

#### **Bước 4: Server Gửi Tin Nhắn Qua Socket**

Khi có tin nhắn mới được tạo (qua HTTP API), server gửi real-time đến tất cả clients liên quan:

```java
// chat-service/src/main/java/com/devteria/chat/service/ChatMessageService.java
public ChatMessageResponse create(ChatMessageRequest request) {
    // ... tạo và lưu tin nhắn vào database ...
    
    // Lấy danh sách userIds trong conversation
    List<String> userIds = conversation.getParticipants().stream()
            .map(ParticipantInfo::getUserId)
            .toList();
    
    // Lấy tất cả WebSocket sessions của các users này
    Map<String, WebSocketSession> webSocketSessions = 
        webSocketSessionRepository.findAllByUserIdIn(userIds)
            .stream()
            .collect(Collectors.toMap(
                WebSocketSession::getSocketSessionId, 
                Function.identity()
            ));
    
    // Chuyển Entity → Response DTO
    ChatMessageResponse chatMessageResponse = 
        chatMessageMapper.toChatMessageResponse(chatMessage);
    
    // Gửi đến tất cả clients đang kết nối
    socketIOServer.getAllClients().forEach(client -> {
        // Kiểm tra xem client này có trong danh sách participants không
        var webSocketSession = webSocketSessions.get(
            client.getSessionId().toString()
        );
        
        if (Objects.nonNull(webSocketSession)) {
            // Set flag "me" để biết tin nhắn này là của mình hay người khác
            chatMessageResponse.setMe(
                webSocketSession.getUserId().equals(userId)
            );
            
            // Serialize object thành JSON string
            String message = objectMapper.writeValueAsString(chatMessageResponse);
            
            // Gửi event "message" với data là JSON string
            client.sendEvent("message", message);
        }
    });
    
    return toChatMessageResponse(chatMessage);
}
```

**Giải thích chi tiết:**

1. **Lấy danh sách participants:**
   ```java
   List<String> userIds = conversation.getParticipants()...
   ```
   → Lấy tất cả userIds trong conversation

2. **Tìm WebSocket sessions:**
   ```java
   Map<String, WebSocketSession> webSocketSessions = ...
   ```
   → Tìm tất cả socket sessions của các users này (đang online)

3. **Duyệt qua tất cả clients:**
   ```java
   socketIOServer.getAllClients().forEach(client -> {
   ```
   → Lấy tất cả clients đang kết nối

4. **Kiểm tra và gửi:**
   ```java
   if (Objects.nonNull(webSocketSession)) {
       chatMessageResponse.setMe(...);  // Set flag
       String message = objectMapper.writeValueAsString(...);  // Object → JSON
       client.sendEvent("message", message);  // Gửi event
   }
   ```

**Lưu ý:**
- `objectMapper.writeValueAsString()`: Chuyển Java Object → JSON string
- `client.sendEvent("message", message)`: Gửi event tên "message" với data là JSON string
- Client phải lắng nghe event "message" để nhận

#### **Bước 5: Client Nhận Tin Nhắn Qua Socket**

```javascript
// web-app/src/pages/Chat.jsx
socketRef.current.on("message", (message) => {
  console.log("New message received:", message);
  
  // message là JSON string, cần parse thành object
  const messageObject = JSON.parse(message);
  console.log("Parsed message object:", messageObject);
  
  // Cập nhật UI
  if (messageObject?.conversationId) {
    handleIncomingMessage(messageObject);
  }
});

// Hàm xử lý tin nhắn mới
const handleIncomingMessage = useCallback((message) => {
  // Thêm vào danh sách messages
  setMessagesMap((prev) => {
    const existingMessages = prev[message.conversationId] || [];
    
    // Kiểm tra trùng lặp
    const messageExists = existingMessages.some((msg) => 
      msg.id === message.id
    );
    
    if (!messageExists) {
      const updatedMessages = [...existingMessages, message].sort(
        (a, b) => new Date(a.createdDate) - new Date(b.createdDate)
      );
      
      return {
        ...prev,
        [message.conversationId]: updatedMessages,
      };
    }
    
    return prev;
  });
  
  // Cập nhật conversation list
  setConversations((prevConversations) => 
    prevConversations.map((conv) =>
      conv.id === message.conversationId
        ? {
            ...conv,
            lastMessage: message.message,
            unread: selectedConversation?.id === message.conversationId 
              ? 0 
              : (conv.unread || 0) + 1,
          }
        : conv
    )
  );
}, [selectedConversation]);
```

**Điều gì xảy ra:**
1. Server gửi event "message" với JSON string
2. Client nhận được trong callback `socket.on("message", ...)`
3. Parse JSON string → JavaScript object
4. Cập nhật state (React)
5. UI tự động re-render với tin nhắn mới

#### **Bước 6: Client Disconnect**

```java
// chat-service/src/main/java/com/devteria/chat/controller/SocketHandler.java
@OnDisconnect
public void clientDisconnected(SocketIOClient client) {
    log.info("Client disConnected: {}", client.getSessionId());
    // Xóa WebSocket session khỏi database
    webSocketSessionService.deleteSession(client.getSessionId().toString());
}
```

```javascript
// web-app/src/pages/Chat.jsx
// Cleanup khi component unmount
return () => {
  if (socketRef.current) {
    console.log("Disconnecting socket...");
    socketRef.current.disconnect();
    socketRef.current = null;
  }
};
```

---

## 4. Ví Dụ Cụ Thể: Gửi Tin Nhắn Chat

### 4.1. Luồng Hoàn Chỉnh

```
┌─────────────┐
│   Client    │
│  (React)    │
└──────┬──────┘
       │
       │ 1. User nhập tin nhắn và click "Send"
       │
       │ 2. HTTP POST /messages/create
       │    Body: {conversationId: "123", message: "Hello"}
       │    Headers: Authorization: Bearer token
       │
       ▼
┌─────────────────────────────────────┐
│   Spring Boot Controller            │
│   ChatMessageController.create()    │
└──────┬──────────────────────────────┘
       │
       │ 3. @RequestBody tự động parse JSON → ChatMessageRequest
       │    request = {conversationId: "123", message: "Hello"}
       │
       ▼
┌─────────────────────────────────────┐
│   Service Layer                     │
│   ChatMessageService.create()       │
└──────┬──────────────────────────────┘
       │
       │ 4. Xác thực user (từ JWT token)
       │ 5. Validate conversation
       │ 6. Lấy user info từ Profile Service
       │
       │ 7. Mapper: ChatMessageRequest → ChatMessage (Entity)
       │    chatMessage = {
       │      conversationId: "123",
       │      message: "Hello",
       │      sender: {userId: "user1", username: "john"},
       │      createdDate: Instant.now()
       │    }
       │
       │ 8. Lưu vào MongoDB
       │    chatMessage = repository.save(chatMessage)
       │
       │ 9. Tìm tất cả WebSocket sessions của participants
       │
       │ 10. Mapper: ChatMessage → ChatMessageResponse
       │     response = {
       │       id: "msg456",
       │       conversationId: "123",
       │       message: "Hello",
       │       me: false,
       │       sender: {...},
       │       createdDate: "2024-01-15T10:30:00Z"
       │     }
       │
       │ 11. Gửi qua Socket.IO
       │     socketIOServer.getAllClients().forEach(client -> {
       │       if (client là participant) {
       │         response.setMe(client.userId == sender.userId);
       │         String json = objectMapper.writeValueAsString(response);
       │         client.sendEvent("message", json);
       │       }
       │     });
       │
       ▼
┌─────────────────────────────────────┐
│   Controller Return                 │
│   ApiResponse<ChatMessageResponse>  │
└──────┬──────────────────────────────┘
       │
       │ 12. Spring Boot tự động serialize → JSON
       │     {
       │       "code": 1000,
       │       "result": { ... ChatMessageResponse ... }
       │     }
       │
       ▼
┌─────────────┐
│   Client    │
│  (React)    │
└──────┬──────┘
       │
       │ 13. Axios nhận JSON response
       │ 14. Tự động parse → JavaScript object
       │
       │ 15. Đồng thời, Socket.IO nhận event "message"
       │     socket.on("message", (jsonString) => {
       │       const message = JSON.parse(jsonString);
       │       handleIncomingMessage(message);
       │     });
       │
       │ 16. Cập nhật UI với tin nhắn mới
```

### 4.2. So Sánh HTTP vs Socket

| **HTTP REST API** | **Socket.IO** |
|-------------------|---------------|
| Request → Response (one-time) | Persistent connection |
| Client phải gọi API mỗi lần | Server push data tự động |
| Dùng để tạo/lấy dữ liệu | Dùng để real-time notification |
| Trong ví dụ: Gửi tin nhắn | Trong ví dụ: Nhận tin nhắn mới |

### 4.3. Tại Sao Cần Cả Hai?

1. **HTTP API (`createMessage`):**
   - Tạo tin nhắn mới
   - Lưu vào database
   - Trả về kết quả cho người gửi

2. **Socket.IO (`message` event):**
   - Thông báo real-time cho tất cả participants
   - Không cần họ phải refresh hoặc poll API
   - Tự động cập nhật UI

---

## 5. Tóm Tắt

### Request → Object:
1. Client gửi JSON trong HTTP body
2. Spring Boot `@RequestBody` + Jackson tự động parse
3. JSON string → Java Object (DTO)

### Object → Response:
1. Service xử lý và tạo Response DTO
2. Controller wrap trong `ApiResponse<T>`
3. Spring Boot + Jackson tự động serialize
4. Java Object → JSON string
5. Client nhận và parse thành JavaScript object

### Socket Flow:
1. Client connect với token
2. Server xác thực và lưu session
3. Khi có sự kiện, server gửi event qua socket
4. Object → JSON string → Socket event
5. Client nhận và parse JSON → Object
6. Cập nhật UI real-time

---

## 6. Các Annotation và Thư Viện Quan Trọng

### Spring Boot:
- `@RequestBody`: Parse JSON → Object
- `@RestController`: Tự động serialize Object → JSON
- `@Valid`: Validation

### Jackson:
- `ObjectMapper.writeValueAsString()`: Object → JSON string
- `ObjectMapper.readValue()`: JSON string → Object
- Tự động hoạt động với Spring Boot

### MapStruct:
- `@Mapper`: Generate code chuyển đổi giữa các object
- `toChatMessageResponse()`: Entity → DTO
- `toChatMessage()`: DTO → Entity

### Socket.IO:
- `@OnConnect`: Xử lý khi client connect
- `@OnDisconnect`: Xử lý khi client disconnect
- `client.sendEvent()`: Gửi event đến client
- `socket.on()`: Lắng nghe event từ server

