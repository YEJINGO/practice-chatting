package com.example.daitgymchatting.chat.repo;

import com.example.daitgymchatting.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> getChatMessagesByRoomId(String roomId);

}
