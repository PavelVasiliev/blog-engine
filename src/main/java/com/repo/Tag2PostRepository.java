package com.repo;

import com.model.entity.Tag2Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface Tag2PostRepository extends JpaRepository<Tag2Post, Integer> {

    @Modifying
    @Transactional
    void deleteAllByPostId(int postId);
}
