package com.example.daitgymchatting.config.jwt;

import com.example.daitgymchatting.config.auth.LoginMember;
import com.example.daitgymchatting.exception.CustomEntryPoint;
import com.example.daitgymchatting.exception.chatException.UnauthorizedException;
import com.example.daitgymchatting.member.entity.Member;
import com.example.daitgymchatting.member.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Key;
import java.util.Date;

import static com.example.daitgymchatting.config.jwt.JwtProperties.*;

@Slf4j
@Component
public class JwtUtils {
    @Value("${jwt.secretKey}")
    private String secretKey;
    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // loginMember 정보를 가지고 AccessToken 생성
    public String generateAccessToken(LoginMember loginMember) {
        return Jwts.builder()
                .claim("id", loginMember.getMember().getId())
                .claim("email", loginMember.getMember().getEmail())
                .claim("nickName", loginMember.getMember().getNickName())
                .claim("role", loginMember.getMember().getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // RefreshToken 생성
    public String generateRefreshToken() {
        return Jwts.builder()
                .setClaims(Jwts.claims())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 파싱하기
    public String parseJwtToken(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);
        if (StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX)) {
            token = token.replace(TOKEN_PREFIX, "");
        }
        return token;
    }

    // 토큰 유효성 검사
    public boolean validationJwt(String token, HttpServletResponse response) throws IOException {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); //토큰에 대한 검증 로직
            return true;
        } catch (ExpiredJwtException e) {
            log.error("토큰 만료 {}", e.getMessage());
            CustomEntryPoint.setResponse(response, "Token Expired", "토큰이 만료되었습니다.");
        } catch (MalformedJwtException e) {
            log.error("토큰 유효성 검사 실패 {}", e.getMessage());
            CustomEntryPoint.setResponse(response, "Invalid Token", "유효하지 않은 토큰");
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 토큰 {}", e.getMessage());
            CustomEntryPoint.setResponse(response, "Unsupported Token", "지원하지 않는 토큰");
        } catch (SignatureException e) {
            log.error("토큰 변조 {}", e.getMessage());
            CustomEntryPoint.setResponse(response, "Token Signature is tempered", "변조된 토큰");
        }

        return false;
    }

    /**
     * Stomp 인증
     * HTTP가 아닌 WS상에서 통신을 한다. 그래서 구현을 이미 해놓은 스프링 시큐리티 JWT 필터를 태우기가 애매하다.(jwt 필터는 HttpServletResponse 객체를 다루기 때문) 그래서 다른 인터셉터를 구현해야 한다.
     */

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            throw UnauthorizedException.of(e.getClass().getName(), "잘못된 JWT 토큰입니다.");
        } catch (ExpiredJwtException e) {
            throw UnauthorizedException.of(e.getClass().getName(), "만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw UnauthorizedException.of(e.getClass().getName(), "지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw UnauthorizedException.of(e.getClass().getName(), "JWT 토큰이 잘못되었습니다.");
        }
    }

    public String getTokenStompHeader(String token) {
        try {
            return getAccessToken(URLDecoder.decode(token, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public String getUid(String token) {

        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        String nickName = claims.get("nickName", String.class);
        return nickName;
    }

    public String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(
                TOKEN_PREFIX)) {
            return authorizationHeader.substring(7);
        }
        return null;
    }


    public LoginMember getMember(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long id = claims.get("id", Long.class);
        String email = claims.get("email", String.class);
        String nickName = claims.get("nickName", String.class);
        String role = claims.get("role", String.class);

        Member member = Member.builder()
                .id(id)
                .email(email)
                .nickName(nickName)
                .role(Role.valueOf(role))
                .build();

        return new LoginMember(member);
    }
}

