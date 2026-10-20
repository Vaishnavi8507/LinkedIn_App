package com.example.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    /**
     * Consume user.created topic
     * Sends a welcome notification
     */
    @KafkaListener(topics = "user.created")
    public void consumeUserCreated(@Payload Map<String, Object> payload) {
        try {
            String userId = (String) payload.get("userId");
            String firstName = (String) payload.get("firstName");

            sendNotification(userId, "Welcome To LinkedIn",
                    String.format("Welcome %s Your acc has been created" +
                            "Start connecting", firstName));

        } catch (Exception e) {
            log.error("Error sending the notification: {}", e.getMessage());

        }
    }

        /**
         * Consume conection.requested event
         * Notify receiver abt connection request
         */
        @KafkaListener(topics = "connection.requested")
        public void consumeConnectionRequest(@Payload Map<String, Object> payload) {
            try {
                String receiverId = (String) payload.get("receiverId");
                String requesterId = (String) payload.get("requesterId");
                sendNotification(receiverId, "New connection request:{}",
                        String.format(
                                "User %s want to connect with you",
                                requesterId));
            } catch (Exception e) {
                log.error("Error sending connection request:{}", e.getMessage());

            }
        }

        @KafkaListener(topics = "connection.accepted")
        public void consumeConnectionAccepted(@Payload Map<String, Object> payload) {
            try {
                String receiverId = (String) payload.get("receiverId");
                String requesterId = (String) payload.get("requesterId");
                sendNotification(receiverId, "Connection Accepted:{}",
                        String.format(
                                "User %s has accepted request" +
                                "You are now connected",
                                requesterId));
            } catch (Exception e) {
                log.error("Error accepting connection request:{}", e.getMessage());
            }
        }

        @KafkaListener(topics = "post.liked")
        public void consumePostLiked(@Payload Map<String, Object> payload) {
            try {
                String authorId = (String) payload.get("authorId");
                String userId = (String) payload.get("userId");
                String postId = (String) payload.get("postId");
                sendNotification(authorId, "Someone liked your post",
                        String.format(
                                "User %s has liked your post", userId, postId));
            } catch (Exception e) {
                log.error("Error liking the post by user:{}", e.getMessage());
            }
        }

        @KafkaListener(topics = "post.commented")
        public void consumePostComment(@Payload Map<String, Object> payload) {
            try {
                String postAuthorId = (String) payload.get("postAuthorId");
                String commenterId = (String) payload.get("commenterId");
                String postId = (String) payload.get("postId");
                sendNotification(postAuthorId, "New comment on your post",
                        String.format(
                                "User %s has commented on your post", commenterId, postId));
            } catch (Exception e) {
                log.error("Error commenting the post:{}", e.getMessage());
            }
        }



        private void sendNotification(String userId, String title, String message) {

            log.info("-----");
            log.info("Notification Service");
            log.info("To User:{}", userId);
            log.info("Title: {}", title);
            log.info("Message:{}", message);
            log.info("------");

        }




}
