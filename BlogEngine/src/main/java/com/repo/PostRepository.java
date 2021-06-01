package com.repo;

import com.model.blog_enum.PostStatus;
import com.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    String OFFSET_LIMIT = "LIMIT ?1, ?2";

    List<Post> findPostsByUserIdOrderByPublicationDate(int id);

    List<Post> findPostsByStatus(PostStatus status);

    List<Post> findPostsByModeratorIdAndStatus(int moderator, PostStatus status);

    @Query(value = "SELECT p.* FROM posts p " +
            "WHERE p.title LIKE %?1% OR p.text LIKE %?1% " +
            "AND p.time <= NOW() AND p.is_active = 1 " +
            "LIMIT ?2, ?3",
            nativeQuery = true)
    List<Post> findByQuery(String query, int offset, int limit);

    @Query(value = "SELECT p.* FROM posts p " +
            "WHERE p.time <= NOW() AND p.is_active = 1",
            nativeQuery = true)
    List<Post> findActiveCurrent();

    @Query(value = "SELECT p.* FROM posts p " +
            "WHERE p.time LIKE :year% " +
            "AND p.time <= NOW() AND p.is_active = 1 AND p.moderation_status = 'NEW'", //ToDo status = 'ACCEPTED'
            nativeQuery = true)
    List<Post> findByYear(@Param("year") String year);

    @Query(value = "SELECT p.* FROM posts p " +
            "WHERE p.time LIKE ?3% " +
            "AND p.time <= NOW() AND p.is_active = 1 AND p.moderation_status = 'NEW' " + //ToDo status = 'ACCEPTED'
            OFFSET_LIMIT,
            nativeQuery = true)
    List<Post> findByDate(int offset, int limit, String date);

    @Query(value = " SELECT p.* FROM tag2post t2p " +
            "INNER JOIN tags t ON t.id = t2p.tag_id " +
            "INNER JOIN posts p ON p.id = t2p.post_id " +
            "WHERE t.name = ?3 AND p.time <= NOW() AND p.is_active = 1 AND p.moderation_status = 'NEW' " + //ToDo status = 'ACCEPTED'
            OFFSET_LIMIT,
            nativeQuery = true)
    List<Post> findByTag(int offset, int limit, String tag);

    @Modifying
    @Transactional
    @Query(value = "UPDATE posts p SET p.view_count = p.view_count + 1 " +
            "WHERE p.id = :id",
            nativeQuery = true)
    void updatePostGetViewed(@Param("id") int id);
}
