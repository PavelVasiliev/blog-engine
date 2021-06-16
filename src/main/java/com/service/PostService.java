package com.service;

import com.api.request.PostRequest;
import com.api.response.PostResponse;
import com.dto.CommentDTO;
import com.dto.PostDTO;
import com.dto.UserDTO;
import com.model.blog_enum.PostStatus;
import com.model.entity.Post;
import com.model.entity.PostVotes;
import com.model.entity.Tag;
import com.model.entity.User;
import com.repo.CommentRepository;
import com.repo.PostRepository;
import com.repo.TagRepository;
import com.repo.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {
    public static final int MIN_TITLE = 3;
    public static final int MIN_TEXT = 50;
    private final String OFFSET_DEFAULT = "0";
    private final String LIMIT_DEFAULT = "10";

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final Tag2PostService tag2PostService;
    private final TagRepository tagRepository;
    private final JdbcTemplate jdbcTemplate;
    @Qualifier("postRepositoryImpl")
    private final PostRepository postRepository;
    private static final Logger logger = LogManager.getLogger(PostService.class);


    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository,
                       CommentRepository commentRepository,
                       Tag2PostService tag2PostService, TagRepository tagRepository,
                       JdbcTemplate jdbcTemplate) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.tag2PostService = tag2PostService;
        this.tagRepository = tagRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Post> findById(int id) {
        return postRepository.findById(id);
    }

    public List<Post> getPostsByUserId(int id) {
        return postRepository.postsByUserId(id);
    }

    public int countActiveCurrentPosts() {
        return postRepository.postsActiveCurrent().size();
    }

    public List<Post> getActivePosts() {
        return postRepository.postsActiveCurrent();
    }

    public void post(PostRequest request, long timestamp) {
        Optional<User> optionalUser = userRepository.findByEmail(AuthService.getCurrentEmail());
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            user = new User();
            userRepository.save(user);
        }

        Post post = new Post(
                user,
                request.getTitle().trim(),
                request.getText().trim(),
                timestamp,
                request.getActive());

        List<String> tagsNames = request.getTags();
        Set<Tag> tags = tag2PostService.updateTagsToPost(tagsNames, post);

        post.setTags(tags);
        postRepository.save(post);
        logger.info(user.getEmail() + " posted " + post.getTitle());
    }

    public void editPost(int id, PostRequest request, long time) {
        Optional<User> optional = userRepository.findByEmail(AuthService.getCurrentEmail());
        User currentUser = new User();
        if (optional.isPresent()) {
            currentUser = optional.get();
        }

        Optional<Post> p = findById(id);
        if (p.isPresent()) {
            Post post = p.get();
            post.update(
                    currentUser,
                    request.getActive(),
                    request.getTitle().trim(),
                    time,
                    request.getText().trim());

            List<String> tagsNames = request.getTags();
            Set<Tag> tags = tag2PostService.updateTagsToPost(tagsNames, post);

            post.setTags(tags);
            postRepository.save(post);
        }
    }

    public PostResponse getResponseByModerationStatus(String postMode) {
        for (PostStatus status : PostStatus.values()) {
            if (status.toString().equalsIgnoreCase(postMode)) {
                return getResponseByModerationStatus(OFFSET_DEFAULT, LIMIT_DEFAULT, postMode);
            }
        }
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND
        );
    }

    public PostResponse getResponseByModerationStatus(String offsetString,
                                                      String limitString,
                                                      String postMode) {
        PostResponse postResponse = new PostResponse();
        int offset = Integer.parseInt(offsetString);
        int limit = Integer.parseInt(limitString) + offset;
        postResponse.setCount(countActiveCurrentPosts());

        PostStatus moderationStatus = PostStatus.valueOf(postMode.toUpperCase());
        switch (moderationStatus) {
            case RECENT: {
                postResponse.setPosts(
                        givePostsDTOs(
                                postRepository.postsByCurrent(offset, limit)));
                return postResponse;
            }
            case EARLY: {
                postResponse.setPosts(
                        givePostsDTOs(
                                postRepository.postsByOld(offset, limit)));
                return postResponse;
            }

            case BEST: {
                postResponse.setPosts(
                        givePostsDTOs(
                                postRepository.postsByScore(offset, limit)));
                return postResponse;
            }
            case POPULAR: {
                postResponse.setPosts(
                        givePostsDTOs(
                                postRepository.postsByPopularity(offset, limit)));
                return postResponse;
            }
        }
        return postResponse;
    }

    public ResponseEntity<PostResponse> getResponseByQuery(String offsetString, String limitString, String query) {
        PostResponse postResponse = new PostResponse();
        int offset = Integer.parseInt(offsetString);
        int limit = Integer.parseInt(limitString) + offset;

        int size = (int) postRepository.streamByQuery(query).count();
        postResponse.setCount(size);

        List<PostDTO> posts = postRepository.streamByQuery(query)
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList())
                .stream().map(this::makeDTOWithAnnounceAndCommentCount)
                .collect(Collectors.toList());
        postResponse.setPosts(posts);
        return ResponseEntity.status(HttpStatus.OK).body(postResponse);
    }

    public List<PostDTO> getPostsByYear(int year) {
        Calendar after = new GregorianCalendar(year, Calendar.JANUARY, 1);
        Calendar before = new GregorianCalendar(year + 1, Calendar.JANUARY, 1);
        return givePostsDTOs(postRepository
                .streamByDateBetween(after.getTime(), before.getTime()).collect(Collectors.toList()));
    }

    public PostResponse getResponseByDate(String offsetString, String limitString, String date) {
        PostResponse postResponse = new PostResponse();
        int offset = Integer.parseInt(offsetString);
        int limit = Integer.parseInt(limitString) + offset;
        try {
            Date after = CalendarService.FORMAT.parse(date);
            long day = 86_400_000;
            Date before = new Date(after.getTime() + day);

            postResponse.setCount((int) postRepository.streamByDateBetween(after, before)
                    .count());
            postResponse.setPosts(givePostsDTOs(postRepository
                    .streamByDateBetween(after, before)
                    .skip(offset)
                    .limit(limit)
                    .collect(Collectors.toList())));
        } catch (ParseException e) {
            logger.error("Cant parse date - " + date);
        }
        return postResponse;
    }

    public PostResponse getResponseByTag(String offsetString, String limitString, String tag) {
        PostResponse postResponse = new PostResponse();
        int offset = Integer.parseInt(offsetString);
        int limit = Integer.parseInt(limitString) + offset;
        int size = (int) postRepository.streamByTag(tag).count();
        postResponse.setCount(size);
        postResponse.setPosts(
                givePostsDTOs(
                        postRepository.streamByTag(tag)
                                .skip(offset)
                                .limit(limit)
                                .collect(Collectors.toList())));
        return postResponse;
    }

    public PostResponse getPostsToModerate(int moderatorId, String offsetString, String limitString, String moderationStatus) {
        PostResponse response = new PostResponse();
        int offset = Integer.parseInt(offsetString);
        int limit = Integer.parseInt(limitString) + offset;

        PostStatus status;
        switch (moderationStatus) {
            case "accepted": {
                status = PostStatus.ACCEPTED;
                response.setPosts(givePostsToModerator(moderatorId, status, offset, limit));
                break;
            }
            case "declined": {
                status = PostStatus.DECLINED;
                response.setPosts(givePostsToModerator(moderatorId, status, offset, limit));
                break;
            }
            default: {
                status = PostStatus.NEW;
                response.setPosts(givePostsToModerator(moderatorId, status, offset, limit));
                break;
            }
        }
        response.setCount(countPostsToModerator(moderatorId, status));
        return response;
    }

    public int countPostsToModerator(int moderatorId, PostStatus status) {
        int result;
        if (status.equals(PostStatus.NEW)) {
            result = (int) postRepository.streamByStatus(status).count();
        } else {
            result = (int) postRepository.streamByModeratorIdAndStatus(moderatorId, status).count();
        }
        return result;
    }

    private List<PostDTO> givePostsToModerator(int moderatorId, PostStatus status, int offset, int limit) {
        List<PostDTO> result = new ArrayList<>();

        if (status.equals(PostStatus.NEW)) {
            result.addAll(postRepository.streamByStatus(status)
                    .skip(offset)
                    .limit(limit)
                    .collect(Collectors.toList())
                    .stream()
                    .map(this::makeDTOWithAnnounceAndCommentCount)
                    .collect(Collectors.toList()));
        } else {
            result.addAll(postRepository.streamByModeratorIdAndStatus(moderatorId, status)
                    .skip(offset)
                    .limit(limit)
                    .collect(Collectors.toList())
                    .stream()
                    .map(this::makeDTOWithAnnounceAndCommentCount)
                    .collect(Collectors.toList()));
        }
        return result;
    }

    public PostResponse getResponseMyPostsByMode(int userId, String mode, String offsetString, String limitString) {
        PostResponse postResponse = new PostResponse();
        List<PostDTO> result = new ArrayList<>();
        int offset = Integer.parseInt(offsetString);
        int limit = Integer.parseInt(limitString) + offset;

        switch (mode) {
            case "inactive": {
                result.addAll(giveMyPosts(userId, null, offset, limit));
                break;
            }
            case "pending": {
                result.addAll(giveMyPosts(userId, PostStatus.NEW, offset, limit));
                break;
            }
            case "declined": {
                result.addAll(giveMyPosts(userId, PostStatus.DECLINED, offset, limit));
                break;
            }
            case "published": {
                result.addAll(giveMyPosts(userId, PostStatus.ACCEPTED, offset, limit));
                break;
            }
        }
        postResponse.setCount(result.size());
        postResponse.setPosts(result);
        return postResponse;
    }

    private List<PostDTO> giveMyPosts(int userId, PostStatus status, int offset, int limit) {
        List<PostDTO> result = new ArrayList<>();
        postRepository.postsByUserId(userId, status, offset, limit)
                .forEach(post -> result.add(makeDTOWithAnnounceAndCommentCount(post)));
        return result;
    }

    public ResponseEntity<PostDTO> getPostDTO(int id) {
        PostDTO result;

        Optional<Post> postOptional = postRepository.optionalPostById(id);
        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new PostDTO());
        }

        Post post = postOptional.get();
        Optional<User> optional = userRepository.findByEmail(AuthService.getCurrentEmail());
        UserDTO userDTO;
        if (optional.isPresent()) {
            User user = optional.get();
            if (user.isModerator()) {
                userDTO = UserDTO.makeModeratorDTO(user, countActiveCurrentPosts());
            } else {
                userDTO = UserDTO.makeSimpleUserDTO(user);
                if (user.getId() != post.getId()) {
                    incrementPostViews(post.getId()); //increment only for registered users
                }
            }
        } else {
            userDTO = new UserDTO();
        }
        result = makeDTOWithTagsAndComments(post, userDTO);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    void incrementPostViews(int postId) {
        String query = "UPDATE posts p SET p.view_count = p.view_count + 1 " +
                "WHERE p.id = " + postId;
        jdbcTemplate.execute(query);
    }

    private List<PostDTO> givePostsDTOs(List<Post> posts) {
        List<PostDTO> result = new ArrayList<>();
        for (Post post : posts) {
            result.add(makeDTOWithAnnounceAndCommentCount(post));
        }
        return result;
    }

    private PostDTO makeDTOWithAnnounceAndCommentCount(Post post) {
        UserDTO user = UserService.getUserDTO(post.getUser());
        int[] likesDislikes = getLikesDislikesViews(post);
        int likeCount = likesDislikes[0];
        int dislikeCount = likesDislikes[1];

        int commentCount = commentRepository.findAllByPostId(post.getId()).size();
        return PostDTO.makePostDTOWithAnnounce(post, user, likeCount, dislikeCount, commentCount);
    }

    private PostDTO makeDTOWithTagsAndComments(Post post, UserDTO currentUser) {
        List<CommentDTO> comments = CommentService.makeDTOList(commentRepository.findAllByPostId(post.getId()));
        Set<String> tags = tagRepository
                .findTagsByPostsIsOrderById(List.of(post)).stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        boolean isActive = post.isPostActive();

        if (currentUser != null) {
            if (currentUser.isModeration() || currentUser.getId() == post.getUser().getId()) {
                isActive = true;
            }
        }
        UserDTO user = UserService.getUserDTO(post.getUser());
        int[] likesDislikesViews = getLikesDislikesViews(post);
        int likeCount = likesDislikesViews[0];
        int dislikeCount = likesDislikesViews[1];
        int viewCount = likesDislikesViews[2];
        return PostDTO.makePostDTOWithTagsComments(
                post, user, likeCount, dislikeCount, comments, tags, isActive, viewCount);
    }

    private int[] getLikesDislikesViews(Post post) {
        int likeCount = 0;
        int dislikeCount = 0;
        Set<PostVotes> postVotes = post.getVotes();
        for (PostVotes pv : postVotes) {
            if (pv.getValue() > 0) {
                likeCount++;
            }
            if (pv.getValue() < 0) {
                dislikeCount++;
            }
        }
        int viewCount = post.getViewCount();
        int[] result = new int[3];
        result[0] = likeCount;
        result[1] = dislikeCount;
        result[2] = viewCount;
        return result;
    }
}