package com.example.daitgymchatting.chat.repo;

import com.example.daitgymchatting.chat.entity.ChatRoom;
import com.example.daitgymchatting.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    @Query("SELECT cr FROM ChatRoom cr WHERE (cr.sender = :nickName OR cr.receiver = :nickName)")
    List<ChatRoom> findBySenderOrReceiver(String nickName);
    ChatRoom findBySenderAndReceiver(String nickName, String receiver);

    ChatRoom findByRedisRoomIdAndSenderOrRedisRoomIdAndReceiver(String roomId, String sender, String roomId1, String nickName);

    ChatRoom findByRedisRoomId(String redisRoomId);
}
