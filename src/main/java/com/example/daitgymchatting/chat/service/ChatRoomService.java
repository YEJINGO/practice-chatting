package com.example.daitgymchatting.chat.service;

import com.example.daitgymchatting.chat.dto.*;
import com.example.daitgymchatting.chat.entity.ChatMessage;
import com.example.daitgymchatting.chat.entity.ChatRoom;
import com.example.daitgymchatting.chat.pubsub.RedisSubscriber;
import com.example.daitgymchatting.chat.repo.ChatRoomRepository;
import com.example.daitgymchatting.exception.ErrorCode;
import com.example.daitgymchatting.exception.GlobalException;
import com.example.daitgymchatting.member.entity.Member;
import com.example.daitgymchatting.member.repo.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatRoomService {


    /**
     * 채팅방(topic)에 발행되는 메시지를 처리할 Listner
     */
    private final RedisMessageListenerContainer redisMessageListener;

    /**
     * 구독 처리 서비스
     */
    private final RedisSubscriber redisSubscriber;

    private final ChatMessageService chatMessageService;

    /**
     * Redis
     * RedisTemplate
     */
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private static final String CHAT_MESSAGE = "CHAT_MESSAGE";
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, ChatRoomDto> opsHashChatRoom;
    private HashOperations<String, String, ChatMessage> opsHashChatMessage;

    private final RedisTemplate<String, ChatMessageDto> redisTemplateMessage;

    /**
     * 채팅방의 대화 메시지를 발행하기 위한 redis topic 정보. 서버별로 채팅방에 매치되는 topic정보를 Map에 넣어 roomId로 찾을수 있도록 한다.
     */
    private Map<String, ChannelTopic> topics;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
        topics = new HashMap<>();
    }

    /**
     * 채팅방 생성 : 서버간 채팅방 공유를 위해 redis hash에 저장한다.
     *
     */
    public ChatRoomResponse createChatRoom(Long memberId, ChatMessageRequestDto chatMessageRequestDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        ChatRoom chatRoom = chatRoomRepository.findBySenderAndReceiver(member.getNickName(), chatMessageRequestDto.getReceiver());

        if ((chatRoom == null) || (chatRoom != null && (!member.getNickName().equals(chatRoom.getSender()) && !chatMessageRequestDto.getReceiver().equals(chatRoom.getReceiver())))) {
            ChatRoomDto chatRoomDto = ChatRoomDto.create(chatMessageRequestDto,member);
            opsHashChatRoom.put(CHAT_ROOMS, chatRoomDto.getRedisRoomId(), chatRoomDto);

            ChatRoom saveChatRoom = ChatRoom.builder()
                    .id(chatRoomDto.getId())
                    .roomName(chatRoomDto.getRoomName())
                    .sender(chatRoomDto.getSender())
                    .redisRoomId(chatRoomDto.getRedisRoomId())
                    .member(member)
                    .receiver(chatRoomDto.getReceiver())
                    .build();
            chatRoomRepository.save(saveChatRoom);
            return new ChatRoomResponse(saveChatRoom);

        } else {
            return new ChatRoomResponse(chatRoom.getRedisRoomId());
        }
    }

    /**
     * 사용자 채팅방 가져오기
     * TODO 채팅방 생성을 당했을 때, Sender 와 receiver 가 어떻게 되는지 궁금
     * TODO 전체 조회 시, 최신 메세지 하나 가져오기
     */
    public List<ChatMessageResponseDto> findAllRoomByUser(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        List<ChatRoom> chatRooms = chatRoomRepository.findBySenderOrReceiver(member.getNickName());



        List<ChatMessageResponseDto> chatRoomDtos = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            if (member.getNickName().equals(chatRoom.getSender())) {
                ChatMessageResponseDto chatMessageResponseDto = new ChatMessageResponseDto(
                        chatRoom.getId(),
                        chatRoom.getReceiver(), // roomName
                        chatRoom.getRedisRoomId(),
                        chatRoom.getSender(),
                        chatRoom.getReceiver(),
                        chatMessageService.latestMessage(chatRoom.getRedisRoomId()).getMessage(),
                        chatMessageService.latestMessage(chatRoom.getRedisRoomId()).getTimeDifference()
                );

                chatRoomDtos.add(chatMessageResponseDto);
            } else if(member.getNickName().equals(chatRoom.getReceiver())){
                ChatMessageResponseDto chatMessageResponseDto = new ChatMessageResponseDto(
                        chatRoom.getId(),
                        chatRoom.getSender(),        // roomName
                        chatRoom.getRedisRoomId(),
                        chatRoom.getSender(), //sender
                        chatRoom.getReceiver(),
                        chatMessageService.latestMessage(chatRoom.getRedisRoomId()).getMessage(),
                        chatMessageService.latestMessage(chatRoom.getRedisRoomId()).getTimeDifference()
                );
                chatRoomDtos.add(chatMessageResponseDto);
            }
        }
        return chatRoomDtos;
    }

    public SelectedChatRoomResponse findRoom(String roomId, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        ChatRoom chatRoom =  chatRoomRepository.findByRedisRoomIdAndSenderOrRedisRoomIdAndReceiver(roomId, member.getNickName(), roomId, member.getNickName());
        List<ChatMessageDto> chatMessageDtos = chatMessageService.loadMessage(roomId);

        if (chatRoom == null) {
            throw new IllegalArgumentException("채팅방 없다 이시키야");
        }
        return new SelectedChatRoomResponse(chatRoom,chatMessageDtos);
    }

//    /**
//     * 채팅방 삭제
//     */
//    public void deleteRoom(Long id, Long memberId) {
//        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
//        ChatRoom chatRoom = chatRoomRepository.findByIdAndMemberIdOrIdAndReceiver(id,member.getId(), id, member.getNickName());
//        //sender가 삭제할 경우
//        if (member.getNickName().equals(chatRoom.getSender())) {
//            chatRoomRepository.delete(chatRoom);
//            opsHashChatRoom.delete(CHAT_ROOMS, chatRoom.getRedisRoomId());
//        } else if (member.getNickName().equals(chatRoom.getReceiver())) {
//            chatRoom.setReceiver("Not_Exist_Receiver");
//            chatRoomRepository.save(chatRoom);
//        }
//        chatRoomRepository.delete(chatRoom);
//        opsHashChatRoom.delete(CHAT_ROOMS, chatRoom.getRedisRoomId());
//    }


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
     * redis 채널에서 채팅방 조회
     */
    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }
}