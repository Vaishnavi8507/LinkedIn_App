package com.example.user_service.controller;

import com.example.user_service.dto.UserResponse;
import com.example.user_service.entity.Connection;
import com.example.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Get user profile
     * x-user id -> requesting user from gateway
     * @return
     */
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable String userId, @RequestHeader("X-User-Id") String requestingUserId) {
        log.info("Get profile: {} requested by : {}", userId, requestingUserId);
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    /**
     * Update own profile
     * @param userId
     * @param requestingUserId
     * @return
     */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserResponse> updateProfile(@PathVariable String userId, @RequestHeader("X-User-Id") String requestingUserId, @RequestBody UserResponse request) {
        if(!userId.equals(requestingUserId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PostMapping("/{userId}/profile-photo")
    public ResponseEntity<UserResponse> uploadProfilePhoto(@PathVariable String userId, @RequestHeader("X-User-Id") String requestingUserId, @RequestParam("file")MultipartFile file) {

        if(!userId.equals(requestingUserId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.uploadProfilePhoto(userId, file));
    }

    /**
     * Send connection request
     * Requester id comes from x-user Id header
     * @param targetUserId
     * @param requestingUserId
     * @return
     */
    @PostMapping("/{targetUserId}/connect")
    public ResponseEntity<String> sendConnectionRequest(@PathVariable String targetUserId, @RequestHeader("X-User-Id") String requestingUserId) {
        return ResponseEntity.ok(userService.sendConnectionRequest(targetUserId, requestingUserId));
    }

    @GetMapping("/{userId}/connections/pending")
    public ResponseEntity<List<Connection>> getPendingConnection(@PathVariable String userId) {

        return ResponseEntity.ok(userService.getPendingConnections(userId));
    }

    @PutMapping("/connection/{connectionId}/accept")
    public ResponseEntity<String> acceptConnection(@PathVariable String connectionId, @RequestHeader("X-User-Id") String requestingUserId) {
        return ResponseEntity.ok(userService.acceptConnectionRequest(connectionId));
    }

    public ResponseEntity<List<UserResponse>> getConnections(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getConnections(userId));
    }

}
