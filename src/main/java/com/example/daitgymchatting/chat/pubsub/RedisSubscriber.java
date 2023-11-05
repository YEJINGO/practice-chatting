package com.example.daitgymchatting.chat.pubsub;


import com.example.daitgymchatting.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
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

    /**
     * Redis에서 메시지가 발행(publish)되면 대기하고 있던 onMessage가 해당 메시지를 받아 처리한다.
     * publishMessage : redis에서 발행된 데이터를 받아 deserialize
     * roomMessage :  ChatMessage 객채로 맵핑
     * messagingTemplate.convertAndSend : Websocket 구독자에게 채팅 메시지 Send
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
            ChatMessageDto roomMessageDto = objectMapper.readValue(publishMessage, ChatMessageDto.class);
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessageDto.getRedisRoomId(), roomMessageDto);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
