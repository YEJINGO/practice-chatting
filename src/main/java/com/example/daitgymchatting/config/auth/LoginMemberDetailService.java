package com.example.daitgymchatting.config.auth;

import com.example.daitgymchatting.exception.ErrorCode;
import com.example.daitgymchatting.exception.GlobalException;
import com.example.daitgymchatting.member.entity.Member;
import com.example.daitgymchatting.member.repo.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService 인터페이스는 화면에서 입력한 이용자의 id(username)를 가지고 loadUserByUsername() 메소드를 호출한다.
 * 그리고 DB에 있는 이용자의 정보를 UserDetails 형으로 가져온다.
 * 만약 이용자가 존재하지 않으면 예외를 던진다.
 * UserDetails 를 User 와 Authentication 사이를 채워주는 Adaptor 라고 생각하자
 */

@Service
@RequiredArgsConstructor
public class LoginMemberDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND, "아이디가 일치하지 않습니다"));

        return new LoginMember(member);
    }

}
