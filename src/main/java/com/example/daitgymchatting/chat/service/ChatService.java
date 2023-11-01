package com.example.daitgymchatting.chat.service;

import com.example.daitgymchatting.chat.dto.UserInfoDto;
import com.example.daitgymchatting.chat.entity.ChatMessage;
import com.example.daitgymchatting.chat.entity.ChatRoom;
import com.example.daitgymchatting.chat.exception.ErrorCode;
import com.example.daitgymchatting.chat.exception.GlobalException;
import com.example.daitgymchatting.chat.pubsub.RedisSubscriber;
import com.example.daitgymchatting.chat.repo.ChatMessageRepository;
import com.example.daitgymchatting.chat.repo.ChatRoomRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {


    /**
     * 채팅방(topic)에 발행되는 메시지를 처리할 Listner
     */
    private final RedisMessageListenerContainer redisMessageListener;

    /**
     * 구독 처리 서비스
     */
    private final RedisSubscriber redisSubscriber;

    /**
     * Redis
     * RedisTemplate
     */
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, ChatRoom> opsHashChatRoom;
    /**
     * 채팅룸에 입장한 클라이언트의 sessionId와 채팅룸 id를 맵핑한 정보 저장
     */
    public static final String ENTER_INFO = "ENTER_INFO";

    /**
     * 채팅방의 대화 메시지를 발행하기 위한 redis topic 정보. 서버별로 채팅방에 매치되는 topic정보를 Map에 넣어 roomId로 찾을수 있도록 한다.
     */
    private Map<String, ChannelTopic> topics;
    private HashOperations<String, String, String> hashOpsEnterInfo;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
        hashOpsEnterInfo = redisTemplate.opsForHash();

        topics = new HashMap<>();
    }

    /**
     * 채팅방 생성 : 서버간 채팅방 공유를 위해 redis hash에 저장한다.
     */
    public ChatRoom createChatRoom(String roomName,UserInfoDto userInfoDto) {

        ChatRoom chatRoom = ChatRoom.builder()
                .name(roomName)
                .sender(userInfoDto)
                .build();

        opsHashChatRoom.put(CHAT_ROOMS, chatRoom.getId(), chatRoom);
        chatRoomRepository.save(chatRoom);
        return chatRoom;
    }

    /**
     * 채팅방 입장 : redis에 topic을 만들고 pub/sub 통신을 하기 위해 리스너를 설정한다.
     */
    public void enterChatRoom(String roomId) {
        ChannelTopic topic = topics.get(roomId);
        if (topic == null)
            topic = new ChannelTopic(roomId);
        redisMessageListener.addMessageListener(redisSubscriber, topic);
        topics.put(roomId, topic);
    }

    /**
     * 채팅방 내용 가져오기
     */

    public List<ChatMessage> getListResult(String roomId) {
        return chatMessageRepository.getChatMessagesByRoomId(roomId);
    }


    public ChatRoom findRoomById(String id) {
        return (ChatRoom) chatRoomRepository.findByChatRoomId(id).orElseThrow(() ->
                new GlobalException(ErrorCode.NOT_FOUND_ROOM));
    }

    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }

    public List<ChatRoom> findAllRoom() {
        return chatRoomRepository.findAll();
    }
}