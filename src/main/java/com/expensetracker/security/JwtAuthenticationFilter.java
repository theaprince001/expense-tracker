package com.expensetracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String path = request.getRequestURI();
        final String method = request.getMethod();

        log.info("🔐 JWT Filter - {} {}", method, path);

        // Skip authentication for public endpoints
        if (path.startsWith("/api/auth/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/api-docs/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/test-redis") ||
                path.startsWith("/ws/**"))

        {
            log.debug("Skipping JWT filter for public endpoint: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("No Bearer token found for protected endpoint: {}", path);
            // Don't block - let Spring Security handle authentication failure
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        if (tokenBlacklistService.isBlacklisted(jwt)) {
            log.warn("JWT token is blacklisted");
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String userEmail = jwtService.extractUsername(jwt);
            log.info("Extracted user from token: {}", userEmail);

            if (StringUtils.hasText(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {

                log.info("Loading user details for: {}", userEmail);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);
                log.info("User loaded - Username: {}, Authorities: {}",
                        userDetails.getUsername(), userDetails.getAuthorities());

                // ✅ FIXED: Now calling the correct method with userDetails parameter
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    log.info("✅ Token is valid for user: {}", userEmail);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Clear context if token is invalid
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (Exception e) {
            // Clear security context on any error
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}