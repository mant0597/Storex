package com.storex.storex.config;

import com.storex.storex.entity.User;
import com.storex.storex.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Extract the Authorization Header
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String username;

            // 2. If header is empty or doesn't start with "Bearer ", skip the filter
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Extract the token itself (omit "Bearer " prefix)
                jwt = authHeader.substring(7);
                username = jwtUtil.extractUsername(jwt);

                // 4. If username is extracted and user is not already authenticated in this thread context
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Optional<User> userOpt = userRepository.findByUsername(username);

                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        
                        // 5. Validate the token claims against our DB user
                        if (jwtUtil.validateToken(jwt, user.getUsername())) {
                            // Ensure role is not null to prevent SimpleGrantedAuthority constructor exception
                            String role = user.getRole() != null ? user.getRole() : "ROLE_USER";

                            // Create authentication token using Spring Security standards
                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    user.getUsername(),
                                    null, // Credential is null because JWT is already verified
                                    Collections.singletonList(new SimpleGrantedAuthority(role))
                            );
                            
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            
                            // 6. Set user in Spring SecurityContextHolder (marks them authenticated)
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // Print the exact verification error to the server console log
            System.err.println("JWT Authentication Filter error: " + ex.getMessage());
            ex.printStackTrace();
        }
        
        // 7. Hand over execution to the next filter in the chain
        filterChain.doFilter(request, response);
    }
}
