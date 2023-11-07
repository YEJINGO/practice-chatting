package com.example.daitgymchatting.member.service;

import com.example.daitgymchatting.common.Response;
import com.example.daitgymchatting.config.auth.LoginMember;
import com.example.daitgymchatting.config.jwt.JwtUtils;
import com.example.daitgymchatting.exception.ErrorCode;
import com.example.daitgymchatting.exception.GlobalException;
import com.example.daitgymchatting.member.dto.request.LoginRequest;
import com.example.daitgymchatting.member.dto.request.SignupRequest;
import com.example.daitgymchatting.member.dto.response.LoginMemberResponse;
import com.example.daitgymchatting.member.entity.Member;
import com.example.daitgymchatting.member.entity.RefreshToken;
import com.example.daitgymchatting.member.repo.MemberRepository;
import com.example.daitgymchatting.member.repo.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Transactional
    public Response<Void> singup(SignupRequest request) {
        // TODO
        /**
         * 이메일이 같지만, ROLE이 다를 경우에는 회원가입 가능하도록 로직 수정하기
         */

        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new GlobalException(ErrorCode.ALREADY_REGISTERED_EMAIL);
        }

        Member member = request.toEntity(bCryptPasswordEncoder.encode(request.getPassword()));
        memberRepository.save(member);

        return Response.success("회원 가입에 성공했습니다.");
    }

    @Transactional
    public Response<LoginMemberResponse> login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        LoginMember loginMember = (LoginMember) authentication.getPrincipal();

        String accessToken = jwtUtils.generateAccessToken(loginMember);
        String refreshToken = jwtUtils.generateRefreshToken();

        refreshTokenRepository.save(RefreshToken.builder()
                .loginMember(loginMember)
                .refreshToken(refreshToken)
                .build());

        LoginMemberResponse result = LoginMemberResponse.builder()
                .loginMember(loginMember)
                .nickName(member.getNickName())
                .imageUrl(member.getImageUrl())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return Response.success("로그인 성공!", result);
    }
}
