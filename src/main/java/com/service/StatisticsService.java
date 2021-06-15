package com.service;

import com.api.response.StatisticsResponse;
import com.model.blog_enum.BlogGlobalSettings;
import com.model.entity.Post;
import com.model.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StatisticsService {
    private final UserService userService;
    private final PostService postService;
    private final VoteService voteService;
    private final SettingsService settingsService;

    public StatisticsService(UserService userService, PostService postService,
                             VoteService voteService, SettingsService settingsService) {
        this.userService = userService;
        this.postService = postService;
        this.voteService = voteService;
        this.settingsService = settingsService;
    }

    public StatisticsResponse getMyStats() {
        Optional<User> optional = userService.getUserByMail(AuthService.getCurrentEmail());
        if(optional.isEmpty()){
            return new StatisticsResponse();
        }
        User user = optional.get();
        List<Post> posts = postService.getPostsByUserId(user.getId());
        int[] likesDislikesViews = voteService.getVotesAndViews(posts);
        return makeStats(posts, likesDislikesViews[0], likesDislikesViews[1], likesDislikesViews[2]);
    }

    public ResponseEntity<StatisticsResponse> getAllStats() {
        Optional<User> optional = userService.getUserByMail(AuthService.getCurrentEmail());
        if (optional.isPresent()) {
            if (optional.get().isModerator()
                    || settingsService.getSetting(BlogGlobalSettings.STATISTICS_IS_PUBLIC).getBooleanValue()) {

                List<Post> posts = postService.getActivePosts();
                int[] likesDislikesViews = voteService.getVotesAndViews(posts);
                return ResponseEntity.ok(
                        makeStats(posts, likesDislikesViews[0], likesDislikesViews[1], likesDislikesViews[2]));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StatisticsResponse());
    }

    private StatisticsResponse makeStats(List<Post> posts, int likes, int dislikes, int views) {
        StatisticsResponse response = new StatisticsResponse(posts.size(), likes, dislikes, views);
        if (!posts.isEmpty()) {
            response.setFirstPublication(posts.get(0).getTime());
        }
        return response;
    }
}
