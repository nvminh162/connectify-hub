# Sơ Đồ Luồng Xử Lý

## 1. Luồng HTTP Request/Response

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (React)                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 1. User Action
                              │    createMessage({conversationId, message})
                              │
                              ▼
                    ┌─────────────────────┐
                    │  JavaScript Object   │
                    │  {                   │
                    │    conversationId,   │
                    │    message           │
                    │  }                   │
                    └──────────┬──────────┘
                               │
                               │ 2. Axios tự động serialize
                               │    Object → JSON string
                               │
                               ▼
                    ┌─────────────────────┐
                    │   HTTP POST Request │
                    │   Body: JSON string │
                    │   Headers:          │
                    │   - Authorization   │
                    │   - Content-Type    │
                    └──────────┬──────────┘
                               │
                               │ 3. Network (HTTP)
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SERVER (Spring Boot)                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 4. Spring Boot nhận request
                              │
                              ▼
                    ┌─────────────────────┐
                    │   @RequestBody      │
                    │   + Jackson         │
                    │   JSON → Object     │
                    └──────────┬──────────┘
                               │
                               │ 5. Tạo ChatMessageRequest
                               │
                               ▼
                    ┌─────────────────────┐
                    │  ChatMessageRequest │
                    │  - conversationId   │
                    │  - message          │
                    └──────────┬──────────┘
                               │
                               │ 6. Controller gọi Service
                               │
                               ▼
                    ┌─────────────────────┐
                    │  ChatMessageService │
                    │  - Validate         │
                    │  - Business Logic   │
                    └──────────┬──────────┘
                               │
                               │ 7. Mapper: Request → Entity
                               │
                               ▼
                    ┌─────────────────────┐
                    │   ChatMessage       │
                    │   (Entity/MongoDB) │
                    └──────────┬──────────┘
                               │
                               │ 8. Save to Database
                               │
                               ▼
                    ┌─────────────────────┐
                    │  Mapper: Entity →   │
                    │  ChatMessageResponse│
                    └──────────┬──────────┘
                               │
                               │ 9. Wrap in ApiResponse
                               │
                               ▼
                    ┌─────────────────────┐
                    │  ApiResponse<        │
                    │    ChatMessageResponse│
                    │  >                   │
                    └──────────┬──────────┘
                               │
                               │ 10. Spring Boot tự động serialize
                               │     Object → JSON string
                               │
                               ▼
                    ┌─────────────────────┐
                    │   HTTP Response     │
                    │   Body: JSON string │
                    │   {                 │
                    │     code: 1000,     │
                    │     result: {...}   │
                    │   }                 │
                    └──────────┬──────────┘
                               │
                               │ 11. Network (HTTP)
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (React)                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 12. Axios nhận response
                              │     Tự động parse JSON → Object
                              │
                              ▼
                    ┌─────────────────────┐
                    │  JavaScript Object   │
                    │  response.data       │
                    └─────────────────────┘
```

## 2. Luồng Socket.IO (Real-time)

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (React)                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 1. Component mount
                              │    useEffect(() => {...})
                              │
                              ▼
                    ┌─────────────────────┐
                    │  io("localhost:8099 │
                    │      ?token=...")   │
                    │  Tạo WebSocket      │
                    │  connection         │
                    └──────────┬──────────┘
                               │
                               │ 2. WebSocket handshake
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SERVER (Spring Boot)                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 3. @OnConnect được gọi
                              │
                              ▼
                    ┌─────────────────────┐
                    │  Lấy token từ       │
                    │  query parameter    │
                    └──────────┬──────────┘
                               │
                               │ 4. Xác thực với Identity Service
                               │
                               ▼
                    ┌─────────────────────┐
                    │  Token hợp lệ?       │
                    │  YES → Lưu session   │
                    │  NO  → Disconnect   │
                    └──────────┬──────────┘
                               │
                               │ 5. Connection established
                               │
                               ▼
                    ┌─────────────────────┐
                    │  WebSocket Session  │
                    │  - sessionId        │
                    │  - userId           │
                    │  Lưu vào MongoDB    │
                    └─────────────────────┘
                              │
                              │ 6. Client connected
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (React)                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ socket.on("connect", ...)
                              │
                              ▼
                    ┌─────────────────────┐
                    │  Socket Connected   │
                    │  Sẵn sàng nhận      │
                    │  real-time events   │
                    └─────────────────────┘
```

## 3. Luồng Gửi Tin Nhắn Qua Socket

```
┌─────────────────────────────────────────────────────────────────┐
│                    SERVER (Spring Boot)                         │
│              ChatMessageService.create()                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 1. Tin nhắn được tạo và lưu DB
                              │
                              ▼
                    ┌─────────────────────┐
                    │  Lấy participants   │
                    │  của conversation   │
                    └──────────┬──────────┘
                               │
                               │ 2. Tìm WebSocket sessions
                               │
                               ▼
                    ┌─────────────────────┐
                    │  Map<sessionId,      │
                    │    WebSocketSession> │
                    └──────────┬──────────┘
                               │
                               │ 3. Entity → Response DTO
                               │
                               ▼
                    ┌─────────────────────┐
                    │  ChatMessageResponse│
                    │  - id               │
                    │  - message           │
                    │  - sender            │
                    │  - createdDate       │
                    └──────────┬──────────┘
                               │
                               │ 4. Duyệt qua tất cả clients
                               │
                               ▼
                    ┌─────────────────────┐
                    │  socketIOServer     │
                    │  .getAllClients()   │
                    │  .forEach(client)   │
                    └──────────┬──────────┘
                               │
                               │ 5. Kiểm tra client có trong
                               │    participants không?
                               │
                               ▼
                    ┌─────────────────────┐
                    │  if (client là      │
                    │      participant)   │
                    │  {                  │
                    │    setMe(flag)      │
                    │    Object → JSON    │
                    │    sendEvent()      │
                    │  }                  │
                    └──────────┬──────────┘
                               │
                               │ 6. Gửi event "message"
                               │
                               ▼
                    ┌─────────────────────┐
                    │  WebSocket Event   │
                    │  Event: "message"   │
                    │  Data: JSON string │
                    └──────────┬──────────┘
                               │
                               │ 7. Network (WebSocket)
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (React)                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 8. socket.on("message", ...)
                              │
                              ▼
                    ┌─────────────────────┐
                    │  Nhận JSON string   │
                    │  JSON.parse()       │
                    └──────────┬──────────┘
                               │
                               │ 9. Parse → JavaScript Object
                               │
                               ▼
                    ┌─────────────────────┐
                    │  handleIncoming     │
                    │  Message(message)   │
                    └──────────┬──────────┘
                               │
                               │ 10. Cập nhật state
                               │
                               ▼
                    ┌─────────────────────┐
                    │  setMessagesMap()   │
                    │  setConversations() │
                    └──────────┬──────────┘
                               │
                               │ 11. React re-render
                               │
                               ▼
                    ┌─────────────────────┐
                    │  UI hiển thị        │
                    │  tin nhắn mới       │
                    └─────────────────────┘
```

## 4. So Sánh HTTP vs Socket

```
┌─────────────────────────────────────────────────────────────┐
│                    HTTP REST API                            │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Client ──[Request]──> Server                               │
│  Client <──[Response]── Server                               │
│                                                              │
│  • One-time request/response                                 │
│  • Client phải gọi mỗi lần                                   │
│  • Dùng để: CREATE, READ, UPDATE, DELETE                    │
│  • Ví dụ: Gửi tin nhắn, lấy danh sách tin nhắn             │
│                                                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    Socket.IO (WebSocket)                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Client ──[Connect]──> Server                               │
│  Client <──[Event]─── Server                                │
│  Client <──[Event]─── Server                                │
│  Client <──[Event]─── Server                                │
│  ...                                                         │
│  Client ──[Disconnect]──> Server                            │
│                                                              │
│  • Persistent connection                                     │
│  • Server push data tự động                                  │
│  • Dùng để: Real-time notifications, live updates           │
│  • Ví dụ: Nhận tin nhắn mới, thông báo real-time            │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## 5. Luồng Hoàn Chỉnh: Gửi và Nhận Tin Nhắn

```
┌─────────────────────────────────────────────────────────────┐
│                    USER ACTION                               │
│              "User A gửi tin nhắn 'Hello'"                   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  STEP 1: HTTP POST /messages/create                         │
│  ────────────────────────────────────────────────────────── │
│  Client → Server:                                           │
│    {conversationId: "123", message: "Hello"}                │
│                                                              │
│  Server xử lý:                                              │
│    1. Parse JSON → ChatMessageRequest                       │
│    2. Validate và lưu vào DB                                │
│    3. Return ApiResponse<ChatMessageResponse>               │
│                                                              │
│  Client nhận response (cho User A)                         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  STEP 2: Socket.IO Event "message"                          │
│  ────────────────────────────────────────────────────────── │
│  Server → Tất cả participants:                             │
│    Event: "message"                                         │
│    Data: {                                                  │
│      id: "msg456",                                           │
│      conversationId: "123",                                  │
│      message: "Hello",                                       │
│      me: false/true,  // false cho User B, true cho User A  │
│      sender: {...},                                         │
│      createdDate: "..."                                     │
│    }                                                         │
│                                                              │
│  User A nhận: me=true  (tin nhắn của mình)                  │
│  User B nhận: me=false (tin nhắn từ User A)                 │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  STEP 3: UI Update                                           │
│  ────────────────────────────────────────────────────────── │
│  User A:                                                    │
│    - Tin nhắn hiển thị bên phải (me=true)                  │
│    - Background: #e3f2fd (xanh nhạt)                       │
│                                                              │
│  User B:                                                    │
│    - Tin nhắn hiển thị bên trái (me=false)                 │
│    - Background: #f5f5f5 (xám)                              │
│    - Avatar của User A hiển thị                             │
│    - Unread count tăng lên (nếu không đang xem)             │
└─────────────────────────────────────────────────────────────┘
```

## 6. Các Object Chuyển Đổi

```
┌─────────────────────────────────────────────────────────────┐
│  REQUEST FLOW                                                │
└─────────────────────────────────────────────────────────────┘

JavaScript Object (Client)
    │
    │ Axios serialize
    ▼
JSON String (HTTP Body)
    │
    │ @RequestBody + Jackson
    ▼
ChatMessageRequest (Java DTO)
    │
    │ Mapper.toChatMessage()
    ▼
ChatMessage (Entity)
    │
    │ Repository.save()
    ▼
ChatMessage (saved in MongoDB)

┌─────────────────────────────────────────────────────────────┐
│  RESPONSE FLOW                                               │
└─────────────────────────────────────────────────────────────┘

ChatMessage (Entity from DB)
    │
    │ Mapper.toChatMessageResponse()
    ▼
ChatMessageResponse (Java DTO)
    │
    │ Wrap in ApiResponse
    ▼
ApiResponse<ChatMessageResponse>
    │
    │ Spring Boot + Jackson serialize
    ▼
JSON String (HTTP Response)
    │
    │ Axios parse
    ▼
JavaScript Object (Client)

┌─────────────────────────────────────────────────────────────┐
│  SOCKET FLOW                                                 │
└─────────────────────────────────────────────────────────────┘

ChatMessage (Entity from DB)
    │
    │ Mapper.toChatMessageResponse()
    ▼
ChatMessageResponse (Java DTO)
    │
    │ Set "me" flag for each client
    ▼
ChatMessageResponse (with me=true/false)
    │
    │ ObjectMapper.writeValueAsString()
    ▼
JSON String
    │
    │ client.sendEvent("message", jsonString)
    ▼
WebSocket Event (to clients)
    │
    │ socket.on("message", ...)
    ▼
JSON String (received by client)
    │
    │ JSON.parse()
    ▼
JavaScript Object (Client)
    │
    │ handleIncomingMessage()
    ▼
React State Update
    │
    │ Re-render
    ▼
UI Update
```

