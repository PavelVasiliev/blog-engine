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

    List<Post> findPostsByIsActiveEqualsAndPublicationDateLessThan(byte isActive, Date date);

    Page<List<Post>> findPostsByIsActiveGreaterThanAndPublicationDateBefore(byte isActive, Date date, Pageable pageable);

    @Query(value = "SELECT p.* FROM posts p " +
            "WHERE moderation_status = ?1 AND p.time <= NOW() AND p.is_active = 1 " +
            OFFSET_LIMIT,
            nativeQuery = true)
    List<Post> findByModerationStatus(int offset, int limit, PostStatus moderation_status); //new, accepted, declined

    @Query(value = "SELECT p.* FROM posts p " +
            "WHERE p.time <= NOW() AND p.is_active = 1 ORDER BY p.time DESC " +
            OFFSET_LIMIT,
            nativeQuery = true)
    List<Post> findRecent(int offset, int limit);

    @Query(value = "SELECT p.* FROM posts p " +
            "WHERE p.time <= NOW() AND p.is_active = 1 ORDER BY p.time ASC " +
            OFFSET_LIMIT,
            nativeQuery = true)
    List<Post> findOld(int offset, int limit);

    @Query(value = "SELECT p.*, IFNULL(SUM(p_v.value), 0) AS 'likes_value' " +
            "FROM posts p " +
            "LEFT JOIN post_votes p_v ON p_v.post_id = p.id " +
            "WHERE p.time <= NOW() AND p.is_active = 1 " +
            "GROUP BY p.id " +
            "ORDER BY likes_value DESC " +
            OFFSET_LIMIT,
            nativeQuery = true)
    List<Post> findBest(int offset, int limit);

    @Query(value = "SELECT p.*, IFNULL(COUNT(p_c.post_id),0) AS 'comments_amount' " +
            "FROM posts p " +
            "LEFT JOIN post_comments p_c ON p_c.post_id = p.id " +
            "WHERE p.time <= NOW() AND p.is_active = 1 " +
            "GROUP BY p.id ORDER BY comments_amount DESC " +
            OFFSET_LIMIT,
            nativeQuery = true)
    List<Post> findPopular(int offset, int limit);

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
