package com.example.daitgymchatting.member.dto.response;

//import com.example.daitgymchatting.member.entity.Role;
//import lombok.Builder;
//import lombok.Getter;
//import readnextday.readnextdayproject.config.auth.LoginMember;
//import readnextday.readnextdayproject.entity.Role;
//
//@Getter
//public class LoginMemberResponse {
//
//    private Long userId;
//
//    private String email;
//
//    private Role role;
//
//    private String accessToken;
//
//    private String refreshToken;
//
//    @Builder
//    public LoginMemberResponse(LoginMember loginMember, String accessToken, String refreshToken) {
//        this.userId = loginMember.getMember().getId();
//        this.email= loginMember.getMember().getEmail();
//        this.role = loginMember.getMember().getRole();
//        this.accessToken = accessToken;
//        this.refreshToken = refreshToken;
//    }
//}