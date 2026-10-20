package com.example.search_service.service;


import com.example.search_service.model.PostDoc;
import com.example.search_service.model.UserDoc;
import com.example.search_service.repository.PostSearchRepo;
import com.example.search_service.repository.UserSearchRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchEventConsumer {

    private final UserSearchRepo userSearchRepo;
    private final PostSearchRepo postSearchRepo;

    @KafkaListener(topics = "user.created")
    public void consumeUserCreatedEvent(@Payload Map<String, Object> payload) {
        try{
            log.info("Indexing new user: {}", payload.get("userId"));
            UserDoc doc = new UserDoc();
            doc.setId((String) payload.get("userId"));
            doc.setFirstName((String) payload.get("firstName"));
            doc.setLastName((String) payload.get("lastName"));
            doc.setEmail((String) payload.get("email"));
            doc.setHeadline((String) payload.get("headline"));
            doc.setLocation((String) payload.get("location"));
            userSearchRepo.save(doc);
            log.info("User indexed: {}", payload.get("userId"));


        } catch (Exception e) {
            log.error("Error indexing user:{}", e.getMessage());
        }
    }

    @KafkaListener(topics = "user.updated")
    public void consumeUserUpdatedEvent(@Payload Map<String, Object> payload) {
        try {
            String userId = (String) payload.get("userId");
            log.info("Updating user index:{}", userId);
            userSearchRepo.findById(userId).ifPresent(doc -> {
                doc.setFirstName((String) payload.get("firstName"));
                doc.setLastName((String) payload.get("lastName"));
                doc.setHeadline((String) payload.get("headline"));
                doc.setLocation((String) payload.get("location"));

                if(payload.get("skills") != null) {
                    doc.setSkills((List<String>) payload.get("Skills"));
                }
                userSearchRepo.save(doc);
                log.info("User index updated: {}", userId);
            });
        } catch (Exception e) {
            log.error("Error updating the user index:{}", e.getMessage());
        }
    }

    /**
     * Consume post.created event
     *
     */
    @KafkaListener(topics = "post.created")
    public void consumePostCreatedEvent(@Payload Map<String,Object> payload) {
        try {
            PostDoc doc = new PostDoc();
            doc.setId((String) payload.get("postId"));
            doc.setAuthorId((String) payload.get("authorId"));
            doc.setContent((String) payload.get("content"));
            doc.setImageUrl((String) payload.get("imageUrl"));
            doc.setCreatedAt((String) payload.get("createdAt"));
            postSearchRepo.save(doc);

        } catch (Exception e) {
            log.error("Error indexing the post:{}", e.getMessage());

        }
    }
}
