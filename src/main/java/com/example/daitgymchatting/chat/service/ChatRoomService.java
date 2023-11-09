package com.example.daitgymchatting.chat.service;

import com.example.daitgymchatting.chat.dto.*;
import com.example.daitgymchatting.chat.entity.ChatRoom;
import com.example.daitgymchatting.chat.entity.UsersChattingRoom;
import com.example.daitgymchatting.chat.pubsub.RedisSubscriber;
import com.example.daitgymchatting.chat.repo.ChatRoomRepository;
import com.example.daitgymchatting.chat.repo.UsersChattingRoomRepository;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final RedisTemplate<String, String> redisTemplate;
    private HashOperations<String, String, ChatRoomDto> opsHashChatRoom;

    /**
     * 채팅방의 대화 메시지를 발행하기 위한 redis topic 정보. 서버별로 채팅방에 매치되는 topic정보를 Map에 넣어 roomId로 찾을수 있도록 한다.
     */
    private Map<String, ChannelTopic> topics;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UsersChattingRoomRepository usersChattingRoomRepository;



    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
        topics = new HashMap<>();
    }

    /**
     * 채팅방 생성 : 서버간 채팅방 공유를 위해 redis hash에 저장한다.
     */
    public ChatRoomResponse createChatRoom(Long memberId, ChatMessageRequestDto chatMessageRequestDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        ChatRoom chatRoom = chatRoomRepository.findBySenderAndReceiver(member.getNickName(), chatMessageRequestDto.getReceiver());

        if ((chatRoom == null) || (chatRoom != null && (!member.getNickName().equals(chatRoom.getSender()) && !chatMessageRequestDto.getReceiver().equals(chatRoom.getReceiver())))) {
            ChatRoomDto chatRoomDto = ChatRoomDto.create(chatMessageRequestDto, member);
            opsHashChatRoom.put(CHAT_ROOMS, chatRoomDto.getRedisRoomId(), chatRoomDto);

            ChatRoom saveChatRoom = ChatRoom.builder()
                    .chatRoomDto(chatRoomDto)
                    .member(member)
                    .build();
            chatRoomRepository.save(saveChatRoom);
            usersChattingRoomRepository.save(new UsersChattingRoom(member, saveChatRoom));
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
            ChatMessageDto latestMsg = chatMessageService.latestMessage(chatRoom.getRedisRoomId());
            String msg = (latestMsg != null) ? latestMsg.getMessage() : "";

            if (member.getNickName().equals(chatRoom.getSender())) {
                ChatMessageResponseDto chatMessageResponseDto = new ChatMessageResponseDto(
                        chatRoom.getId(),
                        chatRoom.getReceiver(), // roomName
                        chatRoom.getRedisRoomId(),
                        chatRoom.getSender(),
                        chatRoom.getReceiver(),
                        msg
                );
                chatRoomDtos.add(chatMessageResponseDto);
            } else if (member.getNickName().equals(chatRoom.getReceiver())) {
                ChatMessageResponseDto chatMessageResponseDto = new ChatMessageResponseDto(
                        chatRoom.getId(),
                        chatRoom.getSender(),        // roomName
                        chatRoom.getRedisRoomId(),
                        chatRoom.getSender(), //sender
                        chatRoom.getReceiver(),
                        msg

                );
                chatRoomDtos.add(chatMessageResponseDto);
            } else {

            }
        }
        return chatRoomDtos;
    }

    public SelectedChatRoomResponse findRoom(String roomId, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        ChatRoom chatRoom = chatRoomRepository.findByRedisRoomIdAndSenderOrRedisRoomIdAndReceiver(roomId, member.getNickName(), roomId, member.getNickName());
        List<ChatMessageDto> chatMessageDtos = chatMessageService.loadMessage(roomId,memberId);

        if (chatRoom == null) {
            throw new IllegalArgumentException("채팅방 없다 이시키야");
        }
        return new SelectedChatRoomResponse(chatRoom, chatMessageDtos);
    }


    /**
     * redis 채널에서 채팅방 조회
     */
    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }

    public void enterChatRoom(String roomId) {
        ChannelTopic topic = topics.get(roomId);
        if (topic == null)
            topic = new ChannelTopic(roomId);
        redisMessageListener.addMessageListener(redisSubscriber, topic);
        topics.put(roomId, topic);
    }

//    public void updateReadCount(String redisRoomId) {
//        ChatRoomDto chatRoomDto = opsHashChatRoom.get(CHAT_ROOMS, redisRoomId);
//        ChatRoom chatRoom = chatRoomRepository.findByRedisRoomId(redisRoomId);
//        if (chatRoomDto != null) {
//            chatRoomDto.setReadCount(0);
//        } chatRoom.setReadCount(0);
//        chatRoomRepository.save(chatRoom);
//    }

//
//    /**
//     * 채팅방에 메시지 발송
//     */
//    public void sendChatMessage(ChatMessageDto chatMessageDto) {
//        if (MessageType.ENTER.equals(chatMessageDto.getMessageType())) {
//            chatMessageDto.setMessage(chatMessageDto.getSender() + "님이 방에 입장했습니다.");
//            chatMessageDto.setSender("[알림]");
//        } else if (MessageType.LEAVE.equals(chatMessageDto.getMessageType())) {
//            chatMessageDto.setMessage(chatMessageDto.getSender() + "님이 방에서 나갔습니다.");
//            chatMessageDto.setSender("[알림]");
//        }
//        ChannelTopic topic = getTopic(chatMessageDto.getRedisRoomId());
//        redisPublisher.publish(topic, chatMessageDto);
//    }
//
//    /**
    /**
     * 채팅방 입장 : redis에 topic을 만들고 pub/sub 통신을 하기 위해 리스너를 설정한다.
     */
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

//     * destination정보에서 roomId 추출
//     */
//    public String getRoomId(String destination) {
//        int lastIndex = destination.lastIndexOf('/');
//        if (lastIndex != -1)
//            return destination.substring(lastIndex + 1);
//        else
//            return "";

//    }
}