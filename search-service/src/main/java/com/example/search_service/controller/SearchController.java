package com.example.search_service.controller;

import com.example.search_service.model.PostDoc;
import com.example.search_service.model.UserDoc;
import com.example.search_service.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@Slf4j
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    /**
     * Search people by name, headline or location
     */
    @GetMapping("/people")
    public ResponseEntity<List<UserDoc>> searchPeople(@RequestParam String q) {
        return ResponseEntity.ok(searchService.searchUsers(q));
    }

    /**
     * Search people by skills
     *
     */
    @GetMapping("/skills")
    public ResponseEntity<List<UserDoc>> searchByskill(@RequestParam String skill) {
        return ResponseEntity.ok(searchService.searchBySkill(skill));
    }

    /**
     * search post by content
     * @param q
     * @return
     */
    @GetMapping("/posts")
    public ResponseEntity<List<PostDoc>> searchPosts(@RequestParam String q) {
        return ResponseEntity.ok(searchService.searchPosts(q));
    }

}
