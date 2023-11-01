//package com.example.daitgymchatting.member.controller;
//
//import com.example.daitgymchatting.common.Response;
//import com.example.daitgymchatting.member.dto.request.LoginRequest;
//import com.example.daitgymchatting.member.dto.request.SignupRequest;
//import com.example.daitgymchatting.member.dto.response.LoginMemberResponse;
//import com.example.daitgymchatting.member.service.MemberService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/member")
//@RequiredArgsConstructor
//public class MemberController {
//
//    private final MemberService memberService;
//
//    // 1. 회원가입
//    @PostMapping("/signup")
//    public Response<Void> signup(@RequestBody SignupRequest request) {
//        return memberService.singup(request);
//    }
//
//    // 2. 로그인
//    @PostMapping("/login")
//    public Response<LoginMemberResponse> login(@RequestBody LoginRequest request) {
//        return memberService.login(request);
//    }
//
//    @PostMapping("/reissue")
//    public Response<LoginMemberResponse> reissue(@RequestBody RefreshTokenRequest request) {
//        return memberService.reissue(request);
//    }
//}
