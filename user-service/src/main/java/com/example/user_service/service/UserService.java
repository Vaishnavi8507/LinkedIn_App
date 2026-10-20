package com.example.user_service.service;

import com.example.user_service.dto.UserResponse;
import com.example.user_service.entity.Connection;
import com.example.user_service.entity.ConnectionStatus;
import com.example.user_service.entity.User;
import com.example.user_service.repository.ConnectionRepo;
import com.example.user_service.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final ConnectionRepo connectionRepo;
    private final UserRepo userRepo;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private S3Service s3Service;

    private static final String CONNECTION_REQUESTED_TOPIC = "connection.requested";
    private static final String CONNECTION_ACCEPTED_TOPIC = "connection.accepted";
    private static final String USER_UPDATED_TOPIC = "user.updated";

    public String sendConnectionRequest(String receiverId, String requesterId) {
        if(connectionRepo.existsByRequesterIdAndReceiverId(requesterId, receiverId)) {
            throw new RuntimeException("Connection request already sent");
        }
        Connection connection = new Connection();
        connection.setRequesterId(requesterId);
        connection.setReceiverId(receiverId);
        connection.setStatus(ConnectionStatus.PENDING);
        connectionRepo.save(connection);

        Map<String, Object> connectionRequestedEvent = new HashMap<>();
        connectionRequestedEvent.put("requesterId", requesterId);
        connectionRequestedEvent.put("receiverId", receiverId);

        kafkaTemplate.send(CONNECTION_REQUESTED_TOPIC, requesterId, connectionRequestedEvent);
        log.info("Connection request sent: {} -> {}", requesterId, receiverId);
        return "Connection request sent!";
    }

    public String acceptConnectionRequest(String connectionId) {
        Connection connection = connectionRepo.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found:" + connectionId));
        connection.setStatus(ConnectionStatus.CONNECTED);
        connectionRepo.save(connection);

        Map<String, Object> connectionAcceptedEvent = new HashMap<>();
        connectionAcceptedEvent.put("requesterId", connection.getRequesterId());
        connectionAcceptedEvent.put("receiverId", connection.getReceiverId());
        kafkaTemplate.send(CONNECTION_ACCEPTED_TOPIC, connection.getRequesterId(), connectionAcceptedEvent);

        log.info("Connection accepted: {}", connectionId);
        return "Connection Accepted!";
    }

    public List<Connection> getPendingConnections(String userId) {
        return connectionRepo.findByReceiverIdAndStatus(userId, ConnectionStatus.PENDING);
    }

    public List<UserResponse> getConnections(String userId) {
        List<Connection> connections = connectionRepo.findByRequesterIdAndStatus(userId, ConnectionStatus.CONNECTED);
        return connections.stream()
                .map(c -> getUserProfile(c.getReceiverId()))
                .collect(Collectors.toList());
    }

    public UserResponse getUserProfile(String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found:" + userId));
        return mapToResponse(user);
    }

    public UserResponse updateProfile(String userId, UserResponse request) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found:" + userId));
        user.setHeadline(request.getHeadline());
        user.setAbout(request.getAbout());
        user.setLocation(request.getLocation());
        user.setSkills(request.getSkills());
        User savedUser = userRepo.save(user);

        Map<String, Object> userUpdatedEvent = new HashMap<>();
        userUpdatedEvent.put("userId", savedUser.getId());
        userUpdatedEvent.put("firstName", savedUser.getFirstName());
        userUpdatedEvent.put("lastName", savedUser.getLastName());
        userUpdatedEvent.put("skills", savedUser.getSkills());
        userUpdatedEvent.put("headline", savedUser.getHeadline());
        userUpdatedEvent.put("location", savedUser.getLocation());
        userUpdatedEvent.put("about", savedUser.getAbout());
        kafkaTemplate.send(USER_UPDATED_TOPIC, savedUser.getId(), userUpdatedEvent);
        log.info("user.updated event published: {}", savedUser.getId());
        return mapToResponse(savedUser);
    }

    public UserResponse uploadProfilePhoto(String userId, MultipartFile file) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found:" +userId));
        String photoUrl = s3Service.uploadFile(
                file, "profiles/" + userId + "/avatar");
        user.setProfilePhotoUrl(photoUrl);
        User savedUser = userRepo.save(user);
        log.info("Profile photo uploaded for user: {}", userId);
        return mapToResponse(savedUser);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setHeadline(user.getHeadline());
        response.setAbout(user.getAbout());
        response.setLastName(user.getLocation());
        response.setProfilePhotoUrl(user.getProfilePhotoUrl());
        response.setRole(user.getRole());
        response.setCoverPhotoUrl(user.getCoverPhotoUrl());
        response.setSkills(user.getSkills());
        response.setCreatedAt(user.getCreatedAt());
        return response;

    }
}
