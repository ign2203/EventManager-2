package dev.sorokin.eventnotificator.consumer.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER = HttpHeaders.AUTHORIZATION;

    private final JwtTokenManager jwtTokenManager;

    public JwtTokenFilter(JwtTokenManager jwtTokenManager) {
        this.jwtTokenManager = jwtTokenManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader(AUTH_HEADER);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            log.trace("Request to {} without Authorization header", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        String loginFromToken;
        String roleFromToken;
        Long userIdFromToken;
        try {
            loginFromToken = jwtTokenManager.getLoginFromToken(jwtToken);
            roleFromToken = jwtTokenManager.getRoleFromToken(jwtToken);
            userIdFromToken = jwtTokenManager.getUserIdFromToken(jwtToken);
        } catch (Exception e) {
            log.error("Invalid JWT Token for request {}", request.getRequestURI(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        loginFromToken,
                        null,
                        List.of(new SimpleGrantedAuthority(roleFromToken))
                );
        authToken.setDetails(userIdFromToken);
        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.info("User '{}' authenticated with role '{}' for request {}",
                loginFromToken, roleFromToken, request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}