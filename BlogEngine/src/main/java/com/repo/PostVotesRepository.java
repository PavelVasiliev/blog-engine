package com.repo;

import com.model.entity.PostVotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostVotesRepository extends JpaRepository<PostVotes, Integer> {
    List<PostVotes> findAllByPostId(int postId);

    Optional<PostVotes> findByPostIdAndUserId(int postId, int userId);
}
