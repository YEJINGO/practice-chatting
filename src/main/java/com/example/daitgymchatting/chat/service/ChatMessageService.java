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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


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
        int size = Math.toIntExact(redisTemplate.opsForSet().size(chatroom.getRedisRoomId() + "set"));
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

        redisTemplateMessage.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatMessageDto.class));
        redisTemplateMessage.opsForList().rightPush(chatMessageDto.getRedisRoomId(), chatMessageDto);
        redisTemplateMessage.expire(chatMessageDto.getRedisRoomId(), 60, TimeUnit.MINUTES);

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

        SetOperations<String, Object> setOperations = redisTemplate.opsForSet();
        Long size = setOperations.size(roomId + "set");
        updateReadCount(roomId, size);

        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();
        List<ChatMessageDto> redisMessageList = redisTemplateMessage.opsForList().range(roomId, 0, 99);

        if (redisMessageList == null || redisMessageList.isEmpty() || redisMessageList.size() < 100) {
            List<ChatMessage> dbMessageList = chatMessageRepository.findTop100ByRedisRoomIdOrderByCreatedAtAsc(roomId);

            // 레디스에 값이 일부만 있으면 -> 있는만큼 set, 추가되는건 rightPush
            for (int i = 0; i < redisMessageList.size(); i++) {
                ChatMessageDto chatMessageDto = new ChatMessageDto(dbMessageList.get(i));
                chatMessageDtos.add(chatMessageDto);
                redisTemplateMessage.opsForList().set(roomId, i, chatMessageDto);
            }
            for (int i = redisMessageList.size(); i < dbMessageList.size(); i++) {
                ChatMessageDto chatMessageDto = new ChatMessageDto(dbMessageList.get(i));
                chatMessageDtos.add(chatMessageDto);
                redisTemplateMessage.opsForList().rightPush(roomId, chatMessageDto);
            }

        } else { // 레디스에 값이 충분히 있으면 -> set
            for (int i = 0; i < redisMessageList.size(); i++) {
                if (redisMessageList.get(i).getReadCount() == 1 && size == 2) {
                    redisMessageList.get(i).setReadCount(0);
                    redisTemplateMessage.opsForList().set(roomId, i, redisMessageList.get(i));
                }
            }
            chatMessageDtos.addAll(redisMessageList);
        }
        return chatMessageDtos;
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
        if (!chatMessages.isEmpty()) {
            if (size == 2) {
                for (ChatMessage chatMessage : chatMessages) {
                    chatMessage.setReadCount(0);
                    chatMessageRepository.save(chatMessage);
                }
            } else {
                for (ChatMessage chatMessage : chatMessages) {
                    chatMessage.setReadCount(1);
                    chatMessageRepository.save(chatMessage);

                }
            }
        }
    }

//    public void updateAllReadCountZero(String stringRedisRoomID,String email) {
//        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
//        List<ChatMessage> chatMessages = chatMessageRepository.findAllByRedisRoomId(stringRedisRoomID);
//        for (ChatMessage chatMessage: chatMessages) {
//            if (!Objects.equals(chatMessage.getSender(), member.getNickName())) {
//                chatMessage.setReadCount(0);
//                chatMessageRepository.save(chatMessage);
//
//                ChatMessageDto chatMessageDto = redisTemplateMessage.opsForValue().get(stringRedisRoomID);
//                if (chatMessageDto != null && Objects.equals(member.getNickName(), chatMessageDto.getSender())) {
//                    chatMessageDto.setReadCount(0);
//                    redisTemplateMessage.opsForValue().set(stringRedisRoomID, chatMessageDto);
//                }
//
//            }
//        }
//
//
//    }
}


