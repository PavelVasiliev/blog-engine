package com.repo;

import com.model.entity.Post;
import com.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {

    //ToDo refactor
    @Query(value = "SELECT count(*) FROM tag2post t2p " +
            "INNER JOIN tags t ON t.id = t2p.tag_id " +
            "INNER JOIN posts p ON p.id = t2p.post_id " +
            "WHERE p.time <= NOW() AND p.is_active = 1 " +
            "AND t.name = ?1",
            nativeQuery = true)
    int countTagsByName(String name);

    List<Tag> findTagsByPostsIsOrderById(List<Post> posts);

    Optional<Tag> findTagByName(String name);

    @Modifying
    @Transactional
    void deleteTagByName(String name);
}

