package com.example.daitgymchatting.chat.controller;

import com.example.daitgymchatting.chat.dto.UserInfoDto;
import com.example.daitgymchatting.chat.entity.ChatMessage;
import com.example.daitgymchatting.chat.entity.ChatRoom;
import com.example.daitgymchatting.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {


    private final ChatService chatService;

    @GetMapping("/rooms")
    public List<ChatRoom> rooms() {
        return chatService.findAllRoom();
    }

    @PostMapping("/room/{roomName}")
    public ChatRoom createRoom(@PathVariable String roomName, @RequestBody UserInfoDto userInfoDto) {
        return chatService.createChatRoom(roomName,userInfoDto);
    }

    @GetMapping("/room/{roomId}")
    public List<ChatMessage> roomChatMessage(@PathVariable String roomId) {
        return chatService.getListResult(roomId);
    }
}
