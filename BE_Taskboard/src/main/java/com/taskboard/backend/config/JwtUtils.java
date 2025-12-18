package com.taskboard.backend.config;

import java.security.Key; // Import sao (*) để lấy hết các class cần thiết
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    // Khóa bí mật (Giữ nguyên của bạn)
    private static final String SECRET_KEY = "TaskBoardSecretKeyTaskBoardSecretKeyTaskBoardSecretKeyTaskBoardSecretKey";
    
    // Thời gian hết hạn token (24 giờ)
    private static final long EXPIRATION_TIME = 86400000; 

    // 1. Hàm tạo Token (Giữ nguyên code cũ của bạn)
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. Hàm lấy Key để ký (Giữ nguyên code cũ của bạn)
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // --- PHẦN BỔ SUNG QUAN TRỌNG ĐỂ SỬA LỖI ---

    // 3. Lấy Username từ Token (Hàm này JwtAuthFilter đang gọi mà chưa có)
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 4. Kiểm tra Token có hợp lệ không (Hàm này JwtAuthFilter cũng đang gọi)
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }
}