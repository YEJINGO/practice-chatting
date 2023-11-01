package com.example.daitgymchatting.chat.pubsub;


import com.example.daitgymchatting.chat.entity.ChatMessage;
import com.example.daitgymchatting.chat.entity.ChatRoom;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;
    private static final String CHAT_MESSAGE = "ChatMessage";

    private HashOperations<String, String, ChatMessage> opsHashChatMessage;

    /**
     * Redis에서 메시지가 발행(publish)되면 대기하고 있던 onMessage가 해당 메시지를 받아 처리한다.
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // redis에서 발행된 데이터를 받아 deserialize
            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
            // ChatMessage 객채로 맵핑
            ChatMessage roomMessage = objectMapper.readValue(publishMessage, ChatMessage.class);
            // Websocket 구독자에게 채팅 메시지 Send
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getRoomId(), roomMessage);
            opsHashChatMessage.put(CHAT_MESSAGE, roomMessage.getRoomId(), roomMessage);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
//    private final ObjectMapper objectMapper;
//    private final SimpMessageSendingOperations messagingTemplate;
//
//    /**
//     * (1) 메시지 리스터에 메시지가 수신되면 sendMessage 가 수행된다.
//     * (2) ChatMessage 객체로 매핑
//     * (3) convertAndSend : 채팅방을 구독한 클라이언트에게 메시지 발송
//     *
//     */
//    public void sendMessage(String publishMessage) {
//        try {
//            // ChatMessage 객채로 맵핑
//            ChatMessageDto chatMessage = objectMapper.readValue(publishMessage, ChatMessageDto.class);
//            messagingTemplate.convertAndSend("/sub/chat/room/" + chatMessage.getChatRoomId(), chatMessage);
//        } catch (Exception e) {
//            log.error("Exception {}", e);
//        }
//    }
