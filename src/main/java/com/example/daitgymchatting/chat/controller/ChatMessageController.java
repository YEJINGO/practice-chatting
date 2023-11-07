package com.example.daitgymchatting.chat.controller;

import com.example.daitgymchatting.chat.dto.ChatMessageDto;
import com.example.daitgymchatting.chat.entity.MessageType;
import com.example.daitgymchatting.chat.pubsub.RedisPublisher;
import com.example.daitgymchatting.chat.service.ChatMessageService;
import com.example.daitgymchatting.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

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

        ChannelTopic topic = chatRoomService.getTopic(chatMessageDto.getRedisRoomId());
        redisPublisher.publish(topic, chatMessageDto);
        messageService.save(chatMessageDto);
    }

//       @MessageMapping("/message")
//    public void message(ChatMessageDto chatMessageDto) {
//        log.info("채팅 메시지");
//
//        if (MessageType.ENTER.equals(chatMessageDto.getMessageType())) {
//            chatRoomService.enterChatRoom(chatMessageDto.getRedisRoomId());
//            chatMessageDto.setMessage("[알림]");
//            chatMessageDto.setMessage(chatMessageDto.getSender() + "님이 입장하셨습니다.");
//        } else if (MessageType.TALK.equals(chatMessageDto.getMessageType())) {
//            chatRoomService.enterChatRoom(chatMessageDto.getRedisRoomId());
//        }
//
//        chatMessageDto.setCreatedAt(LocalDateTime.now());
//
//        ChannelTopic topic = chatRoomService.getTopic(chatMessageDto.getRedisRoomId());
//        redisPublisher.publish(topic, chatMessageDto);
//        messageService.save(chatMessageDto);
//
//    }
//        /**
//         * 대화 내역 조회
//         */
//        @GetMapping("/room/{roomId}/message")
//        public List<ChatMessageDto> loadMessage (@PathVariable String roomId){
//            return messageService.loadMessage(roomId);
//        }
    }
