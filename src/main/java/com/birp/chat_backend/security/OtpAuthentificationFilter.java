package com.birp.chat_backend.security;

import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import com.birp.chat_backend.services.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OtpAuthentificationFilter extends OncePerRequestFilter {

    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = request.getHeader("X-Otp-Token");
        if (token == null || !sessionService.verifyToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid OTP token");
            return;
        }

        filterChain.doFilter(request, response);
        return;
    }

}
