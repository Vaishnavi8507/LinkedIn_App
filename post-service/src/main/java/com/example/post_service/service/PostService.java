package com.example.post_service.service;

import com.example.post_service.entity.Comment;
import com.example.post_service.entity.Like;
import com.example.post_service.entity.Post;
import com.example.post_service.repository.CommentRepo;
import com.example.post_service.repository.LikeRepo;
import com.example.post_service.repository.PostRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {
    private final PostRepo postRepo;
    private final LikeRepo likeRepo;
    private final CommentRepo commentRepo;
    private final S3Service s3Service;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String POST_CREATED_TOPIC = "post.created";
    private static final String POST_LIKED_TOPIC = "post.liked";
    private static final String POST_COMMENTED_TOPIC = "post.comment";
    /**
     * Create a post
     * Optionally upload image to S3
     * Publish post created event to Kafka
     * Feed Service & and Search Service will consume this
     */
    public Post createPost(String authorId, String content, MultipartFile image) {
        log.info("Creating post for new user: {}", authorId);
        Post post = new Post();
        post.setAuthorId(authorId);
        post.setContent(content);

        if(image != null && !image.isEmpty()) {
            String imageUrl = s3Service.uploadFile(image, "posts/" + authorId);
            post.setImageUrl(imageUrl);
        }
        Post savedPost = postRepo.save(post);
        log.info("Post created: {}", savedPost.getId());

        // Publish to kafka- feed service & search service will consume this

        Map<String, Object> postCreatedEvent = new HashMap<>();
        postCreatedEvent.put("postId", savedPost.getId());
        postCreatedEvent.put("authorId", savedPost.getAuthorId());
        postCreatedEvent.put("content", savedPost.getContent());
        postCreatedEvent.put("imageUrl", savedPost.getImageUrl());
        postCreatedEvent.put("createdAt", savedPost.getCreatedAt().toString());

        kafkaTemplate.send(POST_CREATED_TOPIC, savedPost.getId(), postCreatedEvent);
        log.info("post.created published successfully: {}", savedPost.getId());
        return savedPost;
    }

    public Post getPost(String postId) {
        return postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
    }

    public List<Post> getUserPosts(String userId) {
        return postRepo.findByAuthorIdOrderByCreatedAtDesc(userId);
    }

    /**
     *
     * Like or unlike a post
     *
     * @param postId
     * @param userId
     * @return
     */
    public String likePost(String postId, String userId) {
        Post post = getPost(postId);
        if(likeRepo.existsByPostIdAndUserId(postId, userId)) {
            likeRepo.findByPostIdAndUserId(postId, userId)
                    .ifPresent(likeRepo::delete);
            post.setLikeCount(post.getLikeCount() - 1);
            postRepo.save(post);
            return "Post unliked";
        }

        Like like = new Like();
        like.setPostId(postId);
        like.setUserId(userId);
        likeRepo.save(like);
        post.setLikeCount(post.getLikeCount() + 1);
        postRepo.save(post);

        //Publish post.liked event
        Map<String, Object> postLikedEvent = new HashMap<>();
        postLikedEvent.put("postId", postId);
        postLikedEvent.put("userId", userId);
        postLikedEvent.put("authorId", post.getAuthorId());

        kafkaTemplate.send(POST_LIKED_TOPIC, postId, postLikedEvent);
        return "Post liked";
    }

    /**
     * Add comment to Post
     */
    public Comment addComment(String postId, String authorId, String content) {
        Post post = getPost(postId);
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(authorId);
        comment.setContent(content);

        Comment savedComment = commentRepo.save(comment);
        post.setCommentCount(post.getCommentCount() + 1);
        postRepo.save(post);

        //Publish post.comment topic
        Map<String, Object> postCommentedEvent = new HashMap<>();
        postCommentedEvent.put("postId", postId);
        postCommentedEvent.put("authorId", authorId);
        postCommentedEvent.put("comment",savedComment.getId());
        postCommentedEvent.put("postAuthorId", post.getAuthorId());
        kafkaTemplate.send(POST_COMMENTED_TOPIC, postId, postCommentedEvent);
        return savedComment;
    }

    /**
     * Get comments for post
     * @param postId
     * @return
     */
    public List<Comment> getComments(String postId) {
        return commentRepo.findByPostIdOrderByCreatedAtDesc(postId);
    }

    public String deletePost(String postId, String userId){
        Post post = getPost(postId);
        if(!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("Not authorizes to delete the post");
        }
        postRepo.delete(post);
        log.info("Post deleted: {}", postId);
        return "Deleted post!";
    }
}
