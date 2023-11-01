package com.example.daitgymchatting.chat.controller;

import com.example.daitgymchatting.chat.dto.MessageType;
import com.example.daitgymchatting.chat.entity.ChatMessage;
import com.example.daitgymchatting.chat.pubsub.RedisPublisher;
import com.example.daitgymchatting.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final RedisPublisher redisPublisher;
    private final ChatService chatService;


    /**
     * websocket "/pub/api/chat/message/" 로 들어오는 메시징을 처리한다.
     * message.setSender(slackId) : 로그인 회원 정보로 대화명 설정
     * convertAndSend : Websocket 에 발행된 메시지를 redis 로 발행(publish)
     */
    @MessageMapping("/message")
    public void message(ChatMessage message) {
        log.info("채팅 메시지");
        if (MessageType.ENTER.equals(message.getMessageType())) {
            chatService.enterChatRoom(message.getRoomId());
            message.setMessage("[알림]");
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        }
        chatService.save(message);
        ChannelTopic topic = chatService.getTopic(message.getRoomId());
        redisPublisher.publish(topic, message);
    }
}
