package com.hrgenius.recruitment_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.err.println(">>> [DEBUG] No Bearer token found in Authorization header: " + authHeader);
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            if (!jwtUtils.validateToken(jwt)) {
                System.err.println(">>> [DEBUG] Token validation failed in JwtUtils");
                filterChain.doFilter(request, response);
                return;
            }

            String jti = jwtUtils.extractJti(jwt);
            if (jti != null) {
                Boolean isBlacklisted = redisTemplate.hasKey("blacklist:" + jti);
                if (Boolean.TRUE.equals(isBlacklisted)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Token has been invalidated/blacklisted\"}");
                    return;
                }
            }

            String username = jwtUtils.extractUsername(jwt);
            List<String> rawRoles = jwtUtils.extractRoles(jwt);

            System.out.println(">>> [DEBUG] Parsed username: " + username + " | Roles: " + rawRoles);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                List<SimpleGrantedAuthority> authorities = rawRoles.stream()
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println(">>> [DEBUG] SecurityContext successfully populated for: " + username);
            }
        } catch (Exception ex) {
            System.err.println(">>> [DEBUG] Exception in JwtAuthFilter: " + ex.getClass().getName() + " -> " + ex.getMessage());
            ex.printStackTrace();
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}