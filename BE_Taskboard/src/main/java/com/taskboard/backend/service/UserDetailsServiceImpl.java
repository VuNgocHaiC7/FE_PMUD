package com.taskboard.backend.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.taskboard.backend.entity.User;
import com.taskboard.backend.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tìm user trong DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 2. KIỂM TRA: Nếu tài khoản bị khóa thì không cho đăng nhập
        // enabled = true nghĩa là tài khoản đang hoạt động
        boolean enabled = user.isActive();

        // 3. Trả về đối tượng UserDetails mà Spring Security hiểu
        // Lưu ý: Chúng ta lấy role từ DB và gán cho Spring (không thêm "ROLE_" nếu DB bạn lưu là "ADMIN")
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                enabled,  // enabled - tài khoản có đang hoạt động không
                true,     // accountNonExpired - tài khoản có hết hạn không
                true,     // credentialsNonExpired - mật khẩu có hết hạn không  
                true,     // accountNonLocked - tài khoản có bị khóa không (khác với isActive)
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole())) 
        );
    }
}