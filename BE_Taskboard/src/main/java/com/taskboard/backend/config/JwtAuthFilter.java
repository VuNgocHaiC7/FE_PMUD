package com.taskboard.backend.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.taskboard.backend.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // 1. Lấy token từ header "Authorization"
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String username = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7); // Cắt bỏ chữ "Bearer "
                username = jwtUtils.getUserNameFromJwtToken(token); // Lấy username từ token
            }

            // 2. Nếu có username và chưa được xác thực
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                System.out.println(">>> JwtAuthFilter: User=" + username + ", Authorities=" + userDetails.getAuthorities());

                // 3. Kiểm tra token có hợp lệ không
                if (jwtUtils.validateJwtToken(token)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 4. Set xác thực vào hệ thống (Cho qua)
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println(">>> JwtAuthFilter: Authentication set successfully for " + username);
                }
            }
        } catch (Exception e) {
            System.out.println("Cannot set user authentication: " + e);
        }

        filterChain.doFilter(request, response);
    }
}