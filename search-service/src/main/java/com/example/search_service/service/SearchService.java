package com.example.search_service.service;

import com.example.search_service.model.PostDoc;
import com.example.search_service.model.UserDoc;
import com.example.search_service.repository.PostSearchRepo;
import com.example.search_service.repository.UserSearchRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {
    private final UserSearchRepo userSearchRepo;
    private final PostSearchRepo postSearchRepo;

    public List<UserDoc> searchUsers(String query) {
        log.info("Searching users: {}", query);
        return userSearchRepo.searchUsers(query);
    }

    /**
     * Search user by skill
     */
    public List<UserDoc> searchBySkill(String skill) {
        log.info("Searching users by skill: {}", skill);
        return userSearchRepo.findBySkillsContaining(skill);
    }

    /**
     * Search post by content
     */
    public List<PostDoc> searchPosts(String query) {
        log.info("Searching posts: {}", query);
        return postSearchRepo.searchPosts(query);
    }
}
