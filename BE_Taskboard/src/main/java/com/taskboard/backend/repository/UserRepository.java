package com.taskboard.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taskboard.backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Sau này sẽ dùng hàm này để tìm user khi đăng nhập
    Optional<User> findByUsername(String username);
    
    // Lấy tất cả user, sắp xếp theo ID tăng dần
    List<User> findAllByOrderByIdAsc();
    
    // Tìm kiếm user theo keyword (username hoặc fullName), sắp xếp theo ID
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY u.id ASC")
    List<User> searchUsers(@Param("keyword") String keyword);
}