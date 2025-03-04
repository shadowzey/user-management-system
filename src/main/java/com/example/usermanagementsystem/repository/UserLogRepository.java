package com.example.usermanagementsystem.repository;

import com.example.usermanagementsystem.entity.UserLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Long> {
    
    List<UserLog> findByUsername(String username);
    
    List<UserLog> findByUserId(Long userId);
    
    List<UserLog> findByAction(String action);
    
    List<UserLog> findByTimestampBetween(Instant start, Instant end);
    
    List<UserLog> findByStatus(String status);
}
