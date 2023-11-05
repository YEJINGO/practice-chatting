package com.example.daitgymchatting.chat.service;

import com.example.daitgymchatting.chat.dto.ChatMessageDto;
import com.example.daitgymchatting.chat.entity.ChatMessage;
import com.example.daitgymchatting.chat.repo.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final RedisTemplate<String, ChatMessageDto> redisTemplateMessage;
    private final ChatMessageRepository messageRepository;


    public void save(ChatMessageDto chatMessageDto) {


        ChatMessage chatMessage = ChatMessage.builder()
                .messageType(chatMessageDto.getMessageType())
                .sender(chatMessageDto.getSender())
                .message(chatMessageDto.getMessage())
                .redisRoomId(chatMessageDto.getRedisRoomId())
                .build();
        messageRepository.save(chatMessage);

        /**
         * 1. 직렬화
         * 2. redis 저장
         * 3. expire 를 통해 Key값 만료시키기 : 1시간마다 삭제
         */
        redisTemplateMessage.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatMessageDto.class));
        redisTemplateMessage.opsForList().rightPush(chatMessage.getRedisRoomId(), chatMessageDto);
        redisTemplateMessage.expire(chatMessage.getRedisRoomId(), 60, TimeUnit.MINUTES);
    }

    public List<ChatMessageDto> loadMessage(String roomId) {
        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();

        List<ChatMessageDto> redisMessageList = redisTemplateMessage.opsForList().range(roomId, 0, 99);
        if (redisMessageList == null || redisMessageList.isEmpty()) {
            List<ChatMessage> dbMessageList = messageRepository.findTop100ByRedisRoomIdOrderByCreatedAtAsc(roomId);

            for (ChatMessage chatMessage : dbMessageList) {
                ChatMessageDto chatMessageDto = new ChatMessageDto(chatMessage);
                chatMessageDtos.add(chatMessageDto);
                redisTemplateMessage.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatMessageDto.class));      // 직렬화
                redisTemplateMessage.opsForList().rightPush(roomId, chatMessageDto);
            }
        } else {
            chatMessageDtos.addAll(redisMessageList);
        }
        return chatMessageDtos;
    }

    public ChatMessageDto latestMessage(String roomId) {

        ChatMessageDto latestMessage = redisTemplateMessage.opsForList().index(roomId, -1);

        if (latestMessage == null) {
            ChatMessage dbLatestMessage = messageRepository.findTop1ByRedisRoomIdOrderByCreatedAtDesc(roomId);

            if (dbLatestMessage != null) {
                latestMessage = new ChatMessageDto(dbLatestMessage); // ChatMessage를 ChatMessageDto로 변환
                redisTemplateMessage.opsForList().rightPush(roomId, latestMessage);
            }
        }
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime messageCreatedAt = latestMessage.getCreatedAt();
        Duration timeDifference = Duration.between(messageCreatedAt, currentTime);

        // 날짜 및 시간 차이를 ChatMessageDto에 설정
        latestMessage.setTimeDifference(timeDifference);

        return latestMessage;
    }


}
