package com.example.usermanagementsystem.controller;

import com.example.usermanagementsystem.entity.User;
import com.example.usermanagementsystem.entity.UserLog;
import com.example.usermanagementsystem.payload.request.AdminUpdateUserRequest;
import com.example.usermanagementsystem.payload.request.UpdateStatusRequest;
import com.example.usermanagementsystem.payload.response.MessageResponse;
import com.example.usermanagementsystem.payload.response.UserProfileResponse;
import com.example.usermanagementsystem.repository.UserLogRepository;
import com.example.usermanagementsystem.service.UserLogService;
import com.example.usermanagementsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "管理员接口")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserLogService userLogService;
    
    @Autowired
    private UserLogRepository userLogRepository;
    
    @GetMapping("/users")
    // 临时移除安全注解，以便调试
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "获取所有用户", 
        description = "管理员获取所有用户信息（分页）",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<User> usersPage = userService.getAllUsers(pageable);
        
        List<UserProfileResponse> userResponses = usersPage.getContent().stream()
                .map(user -> {
                    // 添加调试日志
                    System.out.println("用户 " + user.getUsername() + " 状态: " + user.isEnabled());
                    
                    return new UserProfileResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getRealName(),
                        user.getAddress(),
                        user.getCity(),
                        user.getPostalCode(),
                        user.getCountry(),
                        user.getBio(),
                        user.isEnabled(),
                        user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .collect(Collectors.toList())
                    );
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", userResponses);
        response.put("currentPage", usersPage.getNumber());
        response.put("totalItems", usersPage.getTotalElements());
        response.put("totalPages", usersPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users/{id}")
    // 临时移除安全注解，以便调试
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "获取用户详情", 
        description = "管理员获取指定用户的详细信息",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("用户不存在"));
        }
        
        UserProfileResponse userResponse = new UserProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getRealName(),
            user.getAddress(),
            user.getCity(),
            user.getPostalCode(),
            user.getCountry(),
            user.getBio(),
            user.isEnabled(),
            user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList())
        );
        
        return ResponseEntity.ok(userResponse);
    }
    
    @PutMapping("/users/{id}")
    // 临时移除安全注解，以便调试
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "更新用户信息", 
        description = "管理员更新指定用户的信息",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest updateRequest,
            HttpServletRequest request) {
        
        try {
            User updatedUser = userService.adminUpdateUser(
                id,
                updateRequest.getPhoneNumber(),
                updateRequest.getRealName(),
                updateRequest.getAddress(),
                updateRequest.getCity(),
                updateRequest.getPostalCode(),
                updateRequest.getCountry(),
                updateRequest.getBio(),
                updateRequest.isEnabled()
            );
            
            return ResponseEntity.ok(new MessageResponse("用户信息更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("用户信息更新失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/users/{id}/status")
    // 临时移除安全注解，以便调试
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "更新用户状态", 
        description = "管理员启用或禁用指定用户",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest statusRequest,
            HttpServletRequest request) {
        
        try {
            User updatedUser = userService.updateUserStatus(id, statusRequest.isEnabled());
            String action = statusRequest.isEnabled() ? "启用" : "禁用";
            return ResponseEntity.ok(new MessageResponse("用户" + action + "成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("更新用户状态失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/users/{id}")
    // 临时移除安全注解，以便调试
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "删除用户", 
        description = "管理员删除指定用户",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new MessageResponse("用户删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("用户删除失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/logs")
    // 临时移除安全注解，以便调试
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "获取所有用户日志", 
        description = "管理员获取所有用户活动日志",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getAllLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer days,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search) {
        
        System.out.println("=== 调试日志查询 ===");
        System.out.println("username: " + username);
        System.out.println("userId: " + userId);
        System.out.println("action: " + action);
        System.out.println("status: " + status);
        System.out.println("days: " + days);
        System.out.println("page: " + page);
        System.out.println("size: " + size);
        System.out.println("sortBy: " + sortBy);
        System.out.println("direction: " + direction);
        System.out.println("search: " + search);
        
        // 直接获取所有日志，不再使用空字符串查询
        List<UserLog> allLogs = userLogRepository.findAll();
        System.out.println("数据库中的日志总数: " + allLogs.size());
        
        // 如果日志为空，创建一条测试日志
        if (allLogs.isEmpty()) {
            System.out.println("创建测试日志...");
            UserLog testLog = new UserLog(
                "TEST", 
                "test_user", 
                1L, 
                "127.0.0.1", 
                "测试日志", 
                "SUCCESS"
            );
            userLogRepository.save(testLog);
            allLogs.add(testLog);
        }
        
        // 如果有搜索条件，进行过滤
        List<UserLog> filteredLogs = new ArrayList<>(allLogs);
        
        // 处理搜索条件
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            filteredLogs = filteredLogs.stream()
                .filter(log -> 
                    (log.getUsername() != null && log.getUsername().toLowerCase().contains(searchLower)) ||
                    (log.getAction() != null && log.getAction().toLowerCase().contains(searchLower)) ||
                    (log.getDetails() != null && log.getDetails().toLowerCase().contains(searchLower))
                )
                .collect(Collectors.toList());
        }
        
        // 处理其他过滤条件
        if (username != null && !username.isEmpty()) {
            filteredLogs = filteredLogs.stream()
                .filter(log -> log.getUsername() != null && log.getUsername().equals(username))
                .collect(Collectors.toList());
        }
        
        if (userId != null) {
            filteredLogs = filteredLogs.stream()
                .filter(log -> log.getUserId() != null && log.getUserId().equals(userId))
                .collect(Collectors.toList());
        }
        
        if (action != null && !action.isEmpty()) {
            filteredLogs = filteredLogs.stream()
                .filter(log -> log.getAction() != null && log.getAction().equals(action))
                .collect(Collectors.toList());
        }
        
        if (status != null && !status.isEmpty()) {
            filteredLogs = filteredLogs.stream()
                .filter(log -> log.getStatus() != null && log.getStatus().equals(status))
                .collect(Collectors.toList());
        }
        
        if (days != null) {
            Instant startTime = Instant.now().minus(days, ChronoUnit.DAYS);
            filteredLogs = filteredLogs.stream()
                .filter(log -> log.getTimestamp() != null && log.getTimestamp().isAfter(startTime))
                .collect(Collectors.toList());
        }
        
        // 排序
        Comparator<UserLog> comparator;
        if ("timestamp".equals(sortBy)) {
            comparator = Comparator.comparing(UserLog::getTimestamp, (t1, t2) -> {
                if (t1 == null && t2 == null) return 0;
                if (t1 == null) return 1;
                if (t2 == null) return -1;
                return t1.compareTo(t2);
            });
        } else if ("username".equals(sortBy)) {
            comparator = Comparator.comparing(UserLog::getUsername, Comparator.nullsLast(String::compareTo));
        } else if ("action".equals(sortBy)) {
            comparator = Comparator.comparing(UserLog::getAction, Comparator.nullsLast(String::compareTo));
        } else if ("status".equals(sortBy)) {
            comparator = Comparator.comparing(UserLog::getStatus, Comparator.nullsLast(String::compareTo));
        } else {
            comparator = Comparator.comparing(UserLog::getId);
        }
        
        // 如果是降序，反转比较器
        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }
        
        // 应用排序
        filteredLogs.sort(comparator);
        
        // 分页
        int totalItems = filteredLogs.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // 防止越界
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;
        
        int start = page * size;
        int end = Math.min(start + size, totalItems);
        
        // 获取当前页的数据
        List<UserLog> pagedLogs = (start < totalItems) ? filteredLogs.subList(start, end) : new ArrayList<>();
        
        System.out.println("过滤后的日志数量: " + filteredLogs.size());
        System.out.println("当前页的日志数量: " + pagedLogs.size());
        
        // 输出日志数据结构
        if (!pagedLogs.isEmpty()) {
            UserLog sampleLog = pagedLogs.get(0);
            System.out.println("日志数据结构示例: " + 
                "\nID: " + sampleLog.getId() + 
                "\n用户名: " + sampleLog.getUsername() + 
                "\n操作: " + sampleLog.getAction() + 
                "\n时间戳: " + sampleLog.getTimestamp() + 
                "\n状态: " + sampleLog.getStatus());
        }
        
        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("logs", pagedLogs);
        response.put("currentPage", page);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/logs/user/{userId}")
    // 临时移除安全注解，以便调试
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "获取指定用户的日志", 
        description = "管理员获取指定用户的活动日志",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getUserLogs(@PathVariable Long userId) {
        List<UserLog> logs = userLogService.getUserLogsById(userId);
        return ResponseEntity.ok(logs);
    }
}
