package com.example.daitgymchatting.chat.config;

import com.example.daitgymchatting.chat.service.ChatMessageService;
import com.example.daitgymchatting.config.jwt.JwtUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
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

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
class StompHandler implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatMessageService chatMessageService;
    private HashOperations<String, String, String> hashOperations;
    private Map<String, String> sessionId;


    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
        sessionId = new HashMap<>();
    }

    /**
     * Websocket 연결 시 요청 header 의 jwt token 유효성을 검증하는 코드를 추가한다. 유효하지 않은 JWT 토큰일 경우, websocket을 연결하지 않고 예외 처리 한다.
     * headerAccessor : Websocket 프로토콜에서 사용되는 헤더 정보를 추출하기 위해 stompHeaderAccessor 를 사용하여 메시지를 매핑한다.
     * presend() : 메시지가 실제로 채널에 전송되기전에 호출된다. 즉, publisher가 send 하기 전에 호출된다.
     */


    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        String session = (String) headerAccessor.getHeader("simpSessionId");
        handleMessage(headerAccessor.getCommand(), headerAccessor, session);

        return message;
    }

    private void handleMessage(StompCommand stompCommand, StompHeaderAccessor headerAccessor, String session) {
        switch (stompCommand) {

            case CONNECT:
                // 토큰 없을 때 예외처리
                verifyAccessToken(headerAccessor);
                connectToChatRoom(headerAccessor, session);
                break;
            case SUBSCRIBE:
                break;
            case DISCONNECT:
                disConnectToChatRoom(session);
                break;
        }
    }

    private boolean verifyAccessToken(StompHeaderAccessor headerAccessor) {
        String token = headerAccessor.getFirstNativeHeader("Authentication");
        String tokenStompHeader = jwtUtils.getTokenStompHeader(token);
//        jwtUtils.validateToken(tokenStompHeader);
        if (jwtUtils.validateToken(tokenStompHeader) == false) {
            log.info("토큰값이 없습니다.");
            return false;
        }
        return true;
    }

    private void connectToChatRoom(StompHeaderAccessor headerAccessor, String session) {
        ChannelTopic redisRoomId = ChannelTopic.of(headerAccessor.getFirstNativeHeader("RedisRoomId"));
        String stringRedisRoomID = redisRoomId.toString();
        String token = headerAccessor.getFirstNativeHeader("Authentication");
        String tokenStompHeader = jwtUtils.getTokenStompHeader(token);
        String email = jwtUtils.getUid(tokenStompHeader);


        hashOperations.put(session, "RedisRoomId", stringRedisRoomID);
        hashOperations.put(session, "email", email);

        SetOperations<String, Object> setOperations = redisTemplate.opsForSet();
        setOperations.add(stringRedisRoomID + "set", email);
        Long size = setOperations.size(stringRedisRoomID + "set");
        chatMessageService.updateReadCount(stringRedisRoomID, size);
    }

    private void disConnectToChatRoom(String session) {

        String redisRoomId = (String) redisTemplate.opsForHash().get(session, "RedisRoomId");
        String email = (String) redisTemplate.opsForHash().get(session, "email");
        redisTemplate.opsForSet().remove(redisRoomId + "set", email);
    }
}
