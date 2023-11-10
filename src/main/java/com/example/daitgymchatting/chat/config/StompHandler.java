package com.example.daitgymchatting.chat.config;

import com.example.daitgymchatting.chat.pubsub.RedisPublisher;
import com.example.daitgymchatting.chat.service.ChatMessageService;
import com.example.daitgymchatting.config.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import static org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT;

@Slf4j
@Component
@RequiredArgsConstructor
class StompHandler implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatMessageService chatMessageService;
//
//    private final ChatMessageController chatMessageController;

    /**
     * Websocket 연결 시 요청 header 의 jwt token 유효성을 검증하는 코드를 추가한다. 유효하지 않은 JWT 토큰일 경우, websocket을 연결하지 않고 예외 처리 한다.
     * headerAccessor : Websocket 프로토콜에서 사용되는 헤더 정보를 추출하기 위해 stompHeaderAccessor 를 사용하여 메시지를 매핑한다.
     * presend() : 메시지가 실제로 채널에 전송되기전에 호출된다. 즉, publisher가 send 하기 전에 호출된다.
     */


    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        String token = headerAccessor.getFirstNativeHeader("Authentication");
        String session = headerAccessor.getFirstNativeHeader("simpSessionId");
        String redisRoomId = headerAccessor.getFirstNativeHeader("RedisRoomId");

        if (headerAccessor.getCommand() != DISCONNECT) {
            redisTemplate.opsForHash().put("SESSION", session, redisRoomId);
            String tokenStompHeader = jwtUtils.getTokenStompHeader(token);
            String email = jwtUtils.getUid(tokenStompHeader);

            handleMessage(headerAccessor.getCommand(), headerAccessor, email);
        }
        else {
            redisRoomId = (String) redisTemplate.opsForHash().get("SESSION", session);

        }

        return message;

    }
    //7623ab78-20d1-97dd-22de-a1ead8892875
    //simpSessionId -> 7623ab78-20d1-97dd-22de-a1ead8892875
    //simpSessionId -> 7623ab78-20d1-97dd-22de-a1ead8892875

    //simpSessionId -> 7a0c4eb7-fcf4-507f-4a63-dd96b6571a92
    //simpSessionId -> 7a0c4eb7-fcf4-507f-4a63-dd96b6571a92
    private void handleMessage(StompCommand stompCommand, StompHeaderAccessor headerAccessor, String email) {
        switch (stompCommand) {

            case CONNECT:
                connectToChatRoom(headerAccessor, email);
                break;
            case SUBSCRIBE:
                break;
            case SEND:
                verifyAccessToken(headerAccessor);
                break;
        }
    }

    private boolean verifyAccessToken(StompHeaderAccessor headerAccessor) {
        String token = headerAccessor.getFirstNativeHeader("Authentication");
        String tokenStompHeader = jwtUtils.getTokenStompHeader(token);
        if (jwtUtils.validateToken(tokenStompHeader) == false) {
            log.info("토큰값이 없습니다.");
            return false;
        }
        return true;
    }

    private void connectToChatRoom(StompHeaderAccessor headerAccessor, String email) {
        ChannelTopic redisRoomId = ChannelTopic.of(headerAccessor.getFirstNativeHeader("RedisRoomId"));
        String stringRedisRoomID = redisRoomId.toString();

        SetOperations<String, Object> setOperations = redisTemplate.opsForSet();
        setOperations.add(stringRedisRoomID + "set", email);
        Long size = setOperations.size(stringRedisRoomID + "set");
        chatMessageService.updateReadCount(stringRedisRoomID, size);
    }

    private void disConnectToChatRoom(String stringRedisRoomID, String email) {
        redisTemplate.opsForSet().remove(stringRedisRoomID + "set", email);
    }
}
