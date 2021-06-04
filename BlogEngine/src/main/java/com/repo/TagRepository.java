package com.repo;

import com.model.entity.Post;
import com.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {

    List<Tag> findTagsByPostsIsOrderById(List<Post> posts);

    Optional<Tag> findTagByName(String name);

    @Modifying
    @Transactional
    void deleteTagByName(String name);
}

