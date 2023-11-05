package com.example.daitgymchatting.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomEntryPoint implements AuthenticationEntryPoint {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String loginException = authException.getClass().getSimpleName();
        String exception = (String) request.getAttribute("exception");
        if (loginException.equals(UsernameNotFoundException.class.getSimpleName())) {
            setResponse(response, "USER_NOT_FOUND", "존재하지 않는 사용자 입니다");
        } else if (loginException.equals(BadCredentialsException.class.getSimpleName())) {
            setResponse(response, "INVALID_PASSWORD", "비밀번호가 일치하지 않습니다.");
        }

        if (exception == null) {
            setResponse(response, "NON_LOGIN", "토큰이 없습니다.");
        }
    }

    public static void setResponse(HttpServletResponse response, String status, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(Response.error(status, message)));
    }
}

