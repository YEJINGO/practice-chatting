package com.example.daitgymchatting.chat.controller;

import com.example.daitgymchatting.chat.dto.ChatMessageRequestDto;
import com.example.daitgymchatting.chat.dto.ChatMessageResponseDto;
import com.example.daitgymchatting.chat.dto.ChatRoomResponse;
import com.example.daitgymchatting.chat.dto.SelectedChatRoomResponse;
import com.example.daitgymchatting.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {


    private final ChatRoomService chatService;

    /**
     * 채팅방 생성
     */
    @PostMapping("/rooms")
    public ChatRoomResponse createRoom(@RequestParam Long memberId, @RequestBody ChatMessageRequestDto chatMessageRequestDto) {
        return chatService.createChatRoom(memberId, chatMessageRequestDto);
    }


    /**
     * 사용자 관련 모든 채팅방 조회
     */
    @GetMapping("/rooms")
    public List<ChatMessageResponseDto> findAllRoomByUser(@RequestParam Long memberId) {
        return chatService.findAllRoomByUser(memberId);
    }

    /**
     * 사용자 관련 선택된 채팅방 조회
     */
    @GetMapping("/rooms/{roomId}")
    public SelectedChatRoomResponse findRoom(@PathVariable String roomId, @RequestParam Long memberId) {
        return chatService.findRoom(roomId, memberId);
    }

//    /**
//     * 채팅방 삭제
//     */
//    @DeleteMapping("/room/{id}")
//    public void deleteRoom(@PathVariable Long id,@RequestParam Long memberId) {
//        chatService.deleteRoom(id,memberId);
//    }
}
