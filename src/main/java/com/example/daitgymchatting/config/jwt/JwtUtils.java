//package com.example.daitgymchatting.config.jwt;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import io.jsonwebtoken.security.SignatureException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import readnextday.readnextdayproject.config.auth.LoginMember;
//import readnextday.readnextdayproject.entity.Member;
//import readnextday.readnextdayproject.entity.Role;
//import readnextday.readnextdayproject.exception.CustomEntryPoint;
//
//import javax.annotation.PostConstruct;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.net.URLDecoder;
//import java.security.Key;
//import java.util.Date;
//
//import static readnextday.readnextdayproject.config.jwt.JwtProperties.*;
//
//@Slf4j
//@Component
//public class JwtUtils {
//
//    @Value("${jwt.secretKey}")
//    private String secretKey;
//    private Key key;
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    @PostConstruct
//    public void init() {
//        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//        this.key = Keys.hmacShaKeyFor(keyBytes);
//    }
//
//    // loginMember 정보를 가지고 AccessToken 생성
//    public String generateAccessToken(LoginMember loginMember) {
//        return Jwts.builder()
//                .claim("id", loginMember.getMember().getId())
//                .claim("email", loginMember.getMember().getEmail())
//                .claim("slackId", loginMember.getMember().getSlackId())
//                .claim("role", loginMember.getMember().getRole())
//                .setIssuedAt(new Date())
//                .setExpiration(new Date((new Date()).getTime() + ACCESS_TOKEN_EXPIRATION_TIME))
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    // RefreshToken 생성
//    public String generateRefreshToken(){
//        return Jwts.builder()
//                .setClaims(Jwts.claims())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME))
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    // 토큰 파싱하기
//    public String parseJwtToken(HttpServletRequest request) {
//        String token = request.getHeader(HEADER_STRING);
//        if (StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX)) {
//            token = token.replace(TOKEN_PREFIX, "");
//        }
//        return token;
//    }
//
//    // 토큰 유효성 검사
//    public boolean validationJwt(String token, HttpServletResponse response) throws IOException {
//        try {
//            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); //토큰에 대한 검증 로직
//            return true;
//        } catch (ExpiredJwtException e) {
//            log.error("토큰 만료 {}", e.getMessage());
//            CustomEntryPoint.setResponse(response, "Token Expired", "토큰이 만료되었습니다.");
//        } catch (MalformedJwtException e) {
//            log.error("토큰 유효성 검사 실패 {}", e.getMessage());
//            CustomEntryPoint.setResponse(response, "Invalid Token", "유효하지 않은 토큰");
//        } catch (UnsupportedJwtException e) {
//            log.error("지원하지 않는 토큰 {}", e.getMessage());
//            CustomEntryPoint.setResponse(response, "Unsupported Token", "지원하지 않는 토큰");
//        } catch (SignatureException e) {
//            log.error("토큰 변조 {}", e.getMessage());
//            CustomEntryPoint.setResponse(response, "Token Signature is tempered", "변조된 토큰");
//        }
//
//        return false;
//    }
//
//    public boolean stompValidationJwt(String token) throws IOException {
//        try {
//            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); //토큰에 대한 검증 로직
//            return true;
//        } catch (ExpiredJwtException e) {
//            log.error("토큰 만료 {}", e.getMessage());
//        } catch (MalformedJwtException e) {
//            log.error("토큰 유효성 검사 실패 {}", e.getMessage());
//        } catch (UnsupportedJwtException e) {
//            log.error("지원하지 않는 토큰 {}", e.getMessage());
//        } catch (SignatureException e) {
//            log.error("토큰 변조 {}", e.getMessage());
//        }
//
//        return false;
//    }
//
//    public String getTokenStompHeader(String token) {
//        try {
//            return getAccessToken(URLDecoder.decode(token, "UTF-8"));
//        } catch (UnsupportedEncodingException e) {
//            return null;
//        }
//    }
//
//    public String getAccessToken(String authorizationHeader) {
//        if (authorizationHeader != null && authorizationHeader.startsWith(
//                TOKEN_PREFIX)) {
//            return authorizationHeader.substring(7);
//        }
//
//        return null;
//    }
//
//
//    public LoginMember getMember(String token) {
//        Claims claims = Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//
//        Long id = claims.get("id", Long.class);
//        String email = claims.get("email", String.class);
//        String slackId = claims.get("slackId", String.class);
//        String role = claims.get("role", String.class);
//
//        Member member = Member.builder()
//                .id(id)
//                .email(email)
//                .slackId(slackId)
//                .role(Role.valueOf(role))
//                .build();
//
//        return new LoginMember(member);
//    }
//}
//
