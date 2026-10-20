package com.example.user_service.repository;

import com.example.user_service.entity.Connection;
import com.example.user_service.entity.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConnectionRepo extends JpaRepository<Connection, String> {
    boolean existsByRequesterIdAndReceiverId(String requesterId, String receiverId);
    List<Connection> findByRequesterIdAndStatus(String requesterId, ConnectionStatus status);
    List<Connection> findByReceiverIdAndStatus(String receiverId, ConnectionStatus status);
}
