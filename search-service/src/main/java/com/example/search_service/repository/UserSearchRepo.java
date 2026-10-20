package com.example.search_service.repository;

import com.example.search_service.model.UserDoc;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface UserSearchRepo extends ElasticsearchRepository<UserDoc, String> {
    @Query("{\"multi_match\": {\"query\": \"?0\", "+ "\"fields\": [\"firstName\", \"lastName\", "+
            "\"headline\", \"location\"]}}")
    List<UserDoc> searchUsers(String query);

    /**
     * Search by specific skill
     * @param skill
     * @return
     */
    List<UserDoc> findBySkillsContaining(String skill);

}
