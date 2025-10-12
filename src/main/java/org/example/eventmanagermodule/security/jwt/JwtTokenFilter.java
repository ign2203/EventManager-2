package org.example.eventmanagermodule.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.eventmanagermodule.User.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenFilter.class);
    private final JwtTokenManager jwtTokenManager;
    private final UserService userService;
    private static final String BEARER_PREFIX = "Bearer ";

    public JwtTokenFilter(
            JwtTokenManager jwtTokenManager,
            @Lazy UserService userService) {
        this.jwtTokenManager = jwtTokenManager;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.debug("No JWT token found in request to {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        var jwtToken = authorizationHeader.substring(BEARER_PREFIX.length());
        String loginFromToken;
        try {
            loginFromToken = jwtTokenManager.getLoginFromToken(jwtToken);
        } catch (Exception e) {
            log.error("Invalid JWT Token", e);
            filterChain.doFilter(request, response);
            return;
        }
        String roleFromToken = jwtTokenManager.getRoleFromToken(jwtToken);
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        loginFromToken,
                        null,
                        List.of(new SimpleGrantedAuthority(roleFromToken))
                );
        SecurityContextHolder.getContext()
                .setAuthentication(token);
        log.debug("Authenticated user {} for request {}", loginFromToken, request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}