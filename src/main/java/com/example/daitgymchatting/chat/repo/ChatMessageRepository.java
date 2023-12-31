package com.example.daitgymchatting.chat.repo;

import com.example.daitgymchatting.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop100ByRedisRoomIdOrderByCreatedAtAsc(String roomId);

    ChatMessage findTop1ByRedisRoomIdOrderByCreatedAtDesc(String roomId);


    ChatMessage findByRedisRoomIdAndId(String redisRoomId, Long chatMessageId);


    List<ChatMessage> findAllByRedisRoomIdAndReadCountAndSenderNot(String redisRoomId, int readCount, String nickName);
}
