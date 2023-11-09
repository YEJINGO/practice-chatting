package com.example.daitgymchatting.chat.service;

import com.example.daitgymchatting.chat.dto.ChatMessageDto;
import com.example.daitgymchatting.chat.entity.ChatMessage;
import com.example.daitgymchatting.chat.entity.ChatRoom;
import com.example.daitgymchatting.chat.repo.ChatMessageRepository;
import com.example.daitgymchatting.chat.repo.ChatRoomRepository;
import com.example.daitgymchatting.exception.ErrorCode;
import com.example.daitgymchatting.exception.GlobalException;
import com.example.daitgymchatting.member.entity.Member;
import com.example.daitgymchatting.member.repo.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, ChatMessageDto> redisTemplateMessage;
    /**
     * 1. setValueSerializer: 직렬화
     * 2. rightPush: redis 저장
     * 3. expire: Key값 만료시키기 : 1시간마다 삭제
     */
    public ChatMessageDto save(ChatMessageDto chatMessageDto) {
        ChatRoom chatroom = chatRoomRepository.findByRedisRoomId(chatMessageDto.getRedisRoomId());
        int size = Math.toIntExact(redisTemplate.opsForSet().size(chatroom.getRedisRoomId()));
        if (size == 2) {
            chatMessageDto.setReadCount(0);
        } else {
            chatMessageDto.setReadCount(1);
        }
        ChatMessage chatMessage = ChatMessage.builder()
                .sender(chatMessageDto.getSender())
                .chatRoom(chatroom)
                .message(chatMessageDto.getMessage())
                .redisRoomId(chatMessageDto.getRedisRoomId())
                .readCount(chatMessageDto.getReadCount())
                .build();
        chatMessageRepository.save(chatMessage);
        chatMessageDto.setChatMessageId(chatMessage.getId());



        return chatMessageDto;
    }

    /**
     * 메세지 로드하기
     * 1. Redis에서 메세지 100개를 가져온다.
     * 2. Redis에 저장된 메세지가 없으면, chatMessageRepository에서 redisRoomId와 일치하는 메세지 100개를 가지고 온다.
     * 3. 가져온 메세지를 chatMessageDtos에 담는다.
     * 4. load했을 때, memberNickName와 sender가 다를 경우 readCount를 0으로 설정한다. (읽은 메세지의 카운트는 0으로 바꿔준다)
     * <p>
     * modifiedChatMessageDtos 를 새로 생성하지 않으면 ConcurrentModificationException 문제가 발생함 ->
     *
     * @param roomId
     * @return
     */
    public List<ChatMessageDto> loadMessage(String roomId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        String memberNickName = member.getNickName();

        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();

        List<ChatMessageDto> redisMessageList = redisTemplateMessage.opsForList().range(roomId, 0, 99);

        if (redisMessageList == null || redisMessageList.isEmpty() || redisMessageList.size() < 10) {
            List<ChatMessage> dbMessageList = chatMessageRepository.findTop100ByRedisRoomIdOrderByCreatedAtAsc(roomId);

            for (ChatMessage chatMessage : dbMessageList) {
                ChatMessageDto chatMessageDto = new ChatMessageDto(chatMessage);
                chatMessageDtos.add(chatMessageDto);

                redisTemplateMessage.opsForList().rightPush(roomId, chatMessageDto);
            }
        } else {
            chatMessageDtos.addAll(redisMessageList);
        }

        // 수정된 요소를 가지는 새로운 리스트를 생성
        List<ChatMessageDto> modifiedChatMessageDtos = new ArrayList<>(chatMessageDtos);

        Iterator<ChatMessageDto> iterator = modifiedChatMessageDtos.iterator();
        while (iterator.hasNext()) {
            ChatMessageDto chatMessageDto = iterator.next();
            Long chatMessageId = chatMessageDto.getChatMessageId();
            String redisRoomId = chatMessageDto.getRedisRoomId();
            ChatMessage chatMessage = chatMessageRepository.findById(chatMessageId).orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND_ROOM));
            if (!memberNickName.equals(chatMessageDto.getSender())) {
                if (chatMessageDto.getReadCount() == 1) {
                    chatMessageDto.setReadCount(chatMessageDto.getReadCount() - 1);
                    // TODO redis 저장, rdb 저장

                    redisTemplateMessage.opsForList().set(redisRoomId, chatMessageDtos.indexOf(chatMessageDto), chatMessageDto);
                    chatMessage.setReadCount(0);
                    chatMessageRepository.save(chatMessage);
                }
            }
        }

        return modifiedChatMessageDtos;
    }

    /**
     * 최신 메세지 가져오기
     * 1. Redis에 값이 있으면 Redis에서 값 가져오기
     * 2. Redis에 값이 없으면 chatMessageRepository에서 값 가져오기
     */
    public ChatMessageDto latestMessage(String roomId) {

        ChatMessageDto latestMessage = redisTemplateMessage.opsForList().index(roomId, -1);


        if (latestMessage == null) {
            ChatMessage dbLatestMessage = chatMessageRepository.findTop1ByRedisRoomIdOrderByCreatedAtDesc(roomId);


            if (dbLatestMessage != null) {
                latestMessage = new ChatMessageDto(dbLatestMessage);
                redisTemplateMessage.opsForList().rightPush(roomId, latestMessage);
            }
        }
        return latestMessage;
    }

    public void updateReadCount(String redisRoomId, Long size) {
        List<ChatMessage> chatMessages = chatMessageRepository.findAllByRedisRoomId(redisRoomId);
        if (chatMessages.size() != 0) {
            if (size == 2) {
                for (ChatMessage chatMessage : chatMessages) {
                    chatMessage.setReadCount(0);
                }
            } else {
                for (ChatMessage chatMessage : chatMessages) {
                    chatMessage.setReadCount(1);
                }
            }
        }
    }
}

//    public List<ChatMessageDto> loadMessage(String roomId){
//
//        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();
//
//        List<ChatMessageDto> redisMessageList = redisTemplateMessage.opsForList().range(roomId, 0, 99);
//
//        if (redisMessageList == null || redisMessageList.isEmpty()) {
//            List<ChatMessage> dbMessageList = chatMessageRepository.findTop100ByRedisRoomIdOrderByCreatedAtAsc(roomId);
//
//            for (ChatMessage chatMessage : dbMessageList) {
//                ChatMessageDto chatMessageDto = new ChatMessageDto(chatMessage);
//                chatMessageDtos.add(chatMessageDto);
//                redisTemplateMessage.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatMessageDto.class));      // 직렬화
//                redisTemplateMessage.opsForList().rightPush(roomId, chatMessageDto);
//            }
//        } else {
//            chatMessageDtos.addAll(redisMessageList);
//        }
//        return chatMessageDtos;
//    }

