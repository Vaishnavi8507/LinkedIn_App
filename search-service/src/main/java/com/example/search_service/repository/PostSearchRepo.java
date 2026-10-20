package com.example.search_service.repository;

import com.example.search_service.model.PostDoc;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface PostSearchRepo extends ElasticsearchRepository<PostDoc, String> {

    @Query("{\"match\": {\"content\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}")
    List<PostDoc> searchPosts(String query);

}
