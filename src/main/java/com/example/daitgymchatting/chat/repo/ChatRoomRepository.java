package com.example.daitgymchatting.chat.repo;

import com.example.daitgymchatting.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    @Query("select c from ChatRoom c where c.id=:id")
    Optional<ChatRoom> findByChatRoomId(@Param("id") String id);
//    List<ChatRoom> findChatRoomsByCustomer(UserInfoDto customer);
//    List<ChatRoom> findChatRoomsByStore(UserInfoDto store);
}
