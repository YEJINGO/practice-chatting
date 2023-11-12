package com.example.daitgymchatting.chat.controller;

import com.example.daitgymchatting.chat.dto.ChatMessageDto;
import com.example.daitgymchatting.chat.pubsub.RedisPublisher;
import com.example.daitgymchatting.chat.service.ChatMessageService;
import com.example.daitgymchatting.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatMessageController {

    private final RedisPublisher redisPublisher;
    private final ChatRoomService chatRoomService;
    private final ChatMessageService messageService;


    /**
     * websocket "/pub/chat/message/" 로 들어오는 메시징을 처리한다.
     * convertAndSend : Websocket 에 발행된 메시지를 redis 로 발행(publish)
     */
    @MessageMapping("/message")
    public void message(ChatMessageDto chatMessageDto) {
        log.info("채팅 메시지");
        chatRoomService.enterChatRoom(chatMessageDto.getRedisRoomId());
        chatMessageDto.setCreatedAt(LocalDateTime.now());
        ChatMessageDto cmd = chatMessageDto;

        ChannelTopic topic = chatRoomService.getTopic(chatMessageDto.getRedisRoomId());
        if (!Objects.equals(chatMessageDto.getMessageType(), "ENTER")) {
             cmd = messageService.save(chatMessageDto);
        }
        redisPublisher.publish(topic, cmd);
    }

//        /**
//         * 대화 내역 조회
//         */
//        @GetMapping("/room/{roomId}/message")
//        public List<ChatMessageDto> loadMessage (@PathVariable String roomId){
//            return messageService.loadMessage(roomId);
//        }
    }
