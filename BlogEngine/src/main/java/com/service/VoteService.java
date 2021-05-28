package com.service;

import com.api.request.VoteRequest;
import com.model.entity.Post;
import com.model.entity.PostVotes;
import com.model.entity.User;
import com.repo.PostRepository;
import com.repo.PostVotesRepository;
import com.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VoteService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostVotesRepository postVotesRepository;

    @Autowired
    public VoteService(UserRepository userRepository, PostRepository postRepository, PostVotesRepository postVotesRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postVotesRepository = postVotesRepository;
    }

    public boolean vote(byte value, VoteRequest request) {
        boolean result = false;
        Optional<User> current = userRepository.findByEmail(AuthService.getCurrentEmail());
        User user = current.orElseGet(User::new);
        Post post = postRepository.getOne(request.getPostId());

        Optional<PostVotes> vote = postVotesRepository.findByPostIdAndUserId(post.getId(), user.getId());
        if (post.getUser().getId() != user.getId()) {
            if (vote.isEmpty()) {
                postVotesRepository.save(new PostVotes(post, user, value));
                result = true;
            } else if (vote.get().getValue() != value){
                PostVotes v = vote.get();
                v.changeOpinion(value);
                postVotesRepository.save(v);
                result = true;
            }
        }
        return result;
    }

    public int[] getVotesAndViews(List<Post> posts){
        int likes = 0;
        int dislikes = 0;
        int views = 0;
        for(Post post: posts){
            int[] votes = getVotesByPostId(post.getId());
            likes += votes[0];
            dislikes += votes[1];
            views += post.getViewCount();
        }
        int[] result = new int[3];
        result[0] = likes;
        result[1] = dislikes;
        result[2] = views;
        return result;
    }

    private int[] getVotesByPostId(int postId){
        int[] likesDislikes = new int[2];
        int likes = 0;
        int dislikes = 0;
        List<PostVotes> votes = postVotesRepository.findAllByPostId(postId);
        for(PostVotes vote : votes){
            if(vote.getValue() > 0){
                likes++;
            } else {
                dislikes++;
            }
        }
        likesDislikes[0] = likes;
        likesDislikes[1] = dislikes;
        return likesDislikes;
    }
}
