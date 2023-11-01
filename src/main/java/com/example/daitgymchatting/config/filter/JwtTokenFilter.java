//package com.example.daitgymchatting.config.filter;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.filter.OncePerRequestFilter;
//import readnextday.readnextdayproject.config.auth.LoginMember;
//import readnextday.readnextdayproject.config.jwt.JwtUtils;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@RequiredArgsConstructor
//public class JwtTokenFilter extends OncePerRequestFilter {
//
//    private final JwtUtils jwtUtils;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        String token = jwtUtils.parseJwtToken(request);
//
//        if (token != null && jwtUtils.validationJwt(token, response)) {
//            LoginMember loginMember = jwtUtils.getMember(token);
//
//            Authentication authentication = new UsernamePasswordAuthenticationToken(loginMember, null, loginMember.getAuthorities());
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}
