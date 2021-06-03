package com.repo;

import com.model.blog_enum.PostStatus;
import com.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> postsByUserId(int id);

    long countActiveCurrentPosts();

    long countByStatusActiveCurrentPosts(PostStatus status);

    List<Post> postsActiveCurrent();

    List<Post> postsByCurrent(int offset, int limit);

    List<Post> postsByOld(int offset, int limit);

    List<Post> postsByScore(int offset, int limit);

    List<Post> postsByPopularity(int offset, int limit);

    Stream<Post> streamByDate(Date after, Date before);

    Stream<Post> streamByQuery(String query);

    Stream<Post> streamByTag(String tag);

    List<Post> postsByTag(String tag, int offset, int limit);

    List<Post> postsByStatus(PostStatus status, int offset, int limit);

    Stream<Post> streamToModeratorByStatus(int moderatorId, PostStatus status);

    List<Post> postsByUserId(int userId, PostStatus status);

    Optional<Post> optionalPostById(int id);
}