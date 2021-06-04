package com.repo;

import com.model.entity.Tag2Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;

@Repository
public interface Tag2PostRepository extends JpaRepository<Tag2Post, Integer> {

    @Modifying
    @Transactional
    void deleteAllByPostId(int postId);

    int countAllByTagNameLikeAndPostIsActiveAndPostPublicationDateBefore(String name, byte isActive, Date date);
}
