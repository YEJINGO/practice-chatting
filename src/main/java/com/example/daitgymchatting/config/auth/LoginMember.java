package com.example.daitgymchatting.config.auth;

import com.example.daitgymchatting.member.entity.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * UserDetails : 사용자의 정보를 담는 인터페이스, 구현해서 사용하면 됨
 * isAccountNonExpired(), isAccountNonLocked(), isCredentialsNonExpired(), isEnabled()
 * 각각 계정의 만료 여부, 잠긴 계정 여부, 자격 증명의 만료 여부, 계정 활성화 여부를 나타내는 메서드
 * 아래에서 모두 true 를 반환하여 계정이 만료되지 않았고 잠기지 않았으며 자격 증명이 만료되지 않았으며 계정이 활성화되어 있다고 가정
 */
@Getter
@RequiredArgsConstructor
public class LoginMember implements UserDetails {

    private final Member member;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(() -> "ROLE_" + member.getRole());
        return authorities;
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
