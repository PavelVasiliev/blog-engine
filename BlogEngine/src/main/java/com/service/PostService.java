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
import com.repo.PostVotesRepository;
import com.repo.TagRepository;
import com.repo.UserRepository;
import org.jinq.jpa.JinqJPAStreamProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.model.entity.Post.PostCommentsSort;

@Service
public class PostService {
    private final String OFFSET_DEFAULT = "0";
    private final String LIMIT_DEFAULT = "10";

    private final PostVotesRepository postVotesRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final Tag2PostService tag2PostService;
    private final TagRepository tagRepository;

    @PersistenceContext
    private final EntityManager entityManager;
    private final JinqJPAStreamProvider streams;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository,
                       PostVotesRepository postVotesRepository, CommentRepository commentRepository,
                       Tag2PostService tag2PostService, TagRepository tagRepository,
                       EntityManager entityManager) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postVotesRepository = postVotesRepository;
        this.commentRepository = commentRepository;
        this.tag2PostService = tag2PostService;
        this.tagRepository = tagRepository;
        this.entityManager = entityManager;
        streams =
                new JinqJPAStreamProvider(this.entityManager.getMetamodel());
    }

    public List<Post> getPostsByUserId(int id) {
        return streams.streamAll(entityManager, Post.class)
                .filter(p -> p.getUser().getId() == id)
                .collect(Collectors.toList());
    }

    public int countNewActiveCurrentPosts() {
        return (int) streams.streamAll(entityManager, Post.class)
                .filter(Post::isPostActive)
                .filter(p -> p.getPublicationDate().before(new Date()))
                .filter(p -> p.getStatus().equals(PostStatus.NEW))
                .count();
    }

    public List<Post> getActivePosts() {
        return streams.streamAll(entityManager, Post.class)
                .filter(Post::isPostActive)
                .filter(p -> p.getPublicationDate().before(new Date()))
                .collect(Collectors.toList());
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
    }

    public void editPost(int id, PostRequest request, long time) {
        Optional<User> optional = userRepository.findByEmail(AuthService.getCurrentEmail());
        User currentUser = new User();
        if (optional.isPresent()) {
            currentUser = optional.get();
        }

        Optional<Post> p = postRepository.findById(id);
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
                //ToDo 404 page
                HttpStatus.NOT_FOUND
        );
    }

    public PostResponse getResponseByModerationStatus(String offsetString,
                                                      String limitString,
                                                      String postMode) {
        PostResponse postResponse = new PostResponse();
        int offset = Integer.parseInt(offsetString);
        int limit = Integer.parseInt(limitString) + offset;
        postResponse.setCount(countNewActiveCurrentPosts());

        PostStatus moderationStatus = PostStatus.valueOf(postMode.toUpperCase());
        switch (moderationStatus) {
            case RECENT: {
                postResponse.setPosts(givePostsDTOs(streams.streamAll(entityManager, Post.class)
                        .sortedDescendingBy(Post::getPublicationDate)
                        .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                        .filter(p -> p.getStatus().equals(PostStatus.NEW))
                        .skip(offset)
                        .limit(limit)
                        .collect(Collectors.toList())));
                return postResponse;
            }
            case EARLY: {
                postResponse.setPosts(givePostsDTOs(streams.streamAll(entityManager, Post.class)
                        .sortedBy(Post::getPublicationDate)
                        .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                        .filter(p -> p.getStatus().equals(PostStatus.NEW))
                        .skip(offset)
                        .limit(limit)
                        .collect(Collectors.toList())));
                return postResponse;
            }

            //ToDo make it normal
            case BEST: {
                postResponse.setPosts(givePostsDTOs(streams.streamAll(entityManager, Post.class)
                        .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                        .filter(p -> p.getStatus().equals(PostStatus.NEW))
                        .sorted()
                        .skip(offset)
                        .limit(limit)
                        .collect(Collectors.toList())));
                return postResponse;
            }
            case POPULAR: {
                postResponse.setPosts(givePostsDTOs(streams.streamAll(entityManager, Post.class)
                        .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                        .filter(p -> p.getStatus().equals(PostStatus.NEW))
                        .sorted(PostCommentsSort)
                        .skip(offset)
                        .limit(limit)
                        .collect(Collectors.toList())));

                return postResponse;
            }
        }
        return postResponse;
    }

    public ResponseEntity<PostResponse> getResponseByQuery(String offsetString, String limitString, String query) {
        PostResponse postResponse = new PostResponse();
        int offset = Integer.parseInt(offsetString);
        int limit = Integer.parseInt(limitString) + offset;

        int size = (int) streams.streamAll(entityManager, Post.class)
                .where(p -> p.getText().contains(query) || p.getTitle().contains(query))
                .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                .filter(p -> p.getStatus().equals(PostStatus.NEW)).count();
        postResponse.setCount(size);

        List<PostDTO> posts = streams.streamAll(entityManager, Post.class)
                .where(p -> p.getText().contains(query) || p.getTitle().contains(query))
                .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                .filter(p -> p.getStatus().equals(PostStatus.NEW))
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
        return givePostsDTOs(streams.streamAll(entityManager, Post.class)
                .filter(Post::isPostActive)
                .filter(p -> p.getStatus().equals(PostStatus.NEW))
                .filter(post -> post.getPublicationDate().after(after.getTime()))
                .filter(post -> post.getPublicationDate().before(before.getTime()))
                .collect(Collectors.toList()));
    }

    public PostResponse getResponseByDate(String offsetString, String limitString, String date) {
        PostResponse postResponse = new PostResponse();
        int offset = Integer.parseInt(offsetString);
        int limit = Integer.parseInt(limitString) + offset;
        try {
            Date after = CalendarService.FORMAT.parse(date);
            long day = 86_400_000;
            Date before = new Date(after.getTime() + day);

            postResponse.setCount((int) streams.streamAll(entityManager, Post.class)
                    .filter(Post::isPostActive)
                    .filter(p -> p.getStatus().equals(PostStatus.NEW))
                    .filter(post -> post.getPublicationDate().after(after))
                    .filter(post -> post.getPublicationDate().before(before))
                    .count());
            postResponse.setPosts(givePostsDTOs(streams.streamAll(entityManager, Post.class)
                    .filter(Post::isPostActive)
                    .filter(p -> p.getStatus().equals(PostStatus.NEW))
                    .filter(post -> post.getPublicationDate().after(after))
                    .filter(post -> post.getPublicationDate().before(before))
                    .skip(offset)
                    .limit(limit)
                    .collect(Collectors.toList())));
        } catch (ParseException e) {
            e.printStackTrace(); //ToDo logger
        }
        return postResponse;
    }

    public PostResponse getResponseByTag(String offsetString, String limitString, String tag) {
        PostResponse postResponse = new PostResponse();
        int offset = Integer.parseInt(offsetString);
        int limit = Integer.parseInt(limitString) + offset;
        int size = (int) streams.streamAll(entityManager, Post.class)
                .filter(post -> new ArrayList<>(
                        post.getTags()).stream()
                        .map(Tag::getName)
                        .anyMatch(t -> t.contains(tag)))
                .count();
        postResponse.setCount(size);

        postResponse.setPosts(
                givePostsDTOs(
                        streams.streamAll(entityManager, Post.class)
                                .filter(Post::isPostActive)
                                .filter(p -> p.getStatus().equals(PostStatus.NEW))
                                .filter(post -> new ArrayList<>(
                                        post.getTags()).stream()
                                        .map(Tag::getName)
                                        .anyMatch(t -> t.contains(tag)))
                                .skip(offset)
                                .limit(limit)
                                .collect(Collectors.toList())));
        return postResponse;
    }

    public PostResponse getPostsToModerate(int moderatorId, String offsetString, String limitString, String moderationStatus) {
        PostResponse response = new PostResponse();
        int offset = Integer.parseInt(offsetString);
        int limit = Integer.parseInt(limitString) + offset;

        PostStatus status = PostStatus.NEW;
        switch (moderationStatus) {
            case "new": {
                status = PostStatus.NEW;
                response.setPosts(givePostsToModerator(moderatorId, status, offset, limit));
                break;
            }
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
        }
        response.setCount(countPostsToModerator(moderatorId, status));
        return response;
    }

    private int countPostsToModerator(int moderatorId, PostStatus status) {
        int result;
        if (status.equals(PostStatus.NEW)) {
            result = countNewActiveCurrentPosts();
        } else {
            result = (int) streams.streamAll(entityManager, Post.class)
                    .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                    .filter(p -> p.getModerator() != null && p.getModerator().getId() == moderatorId)
                    .filter(p -> p.getStatus().equals(status))
                    .count();
        }
        return result;
    }

    private List<PostDTO> givePostsToModerator(int moderatorId, PostStatus status, int offset, int limit) {
        List<PostDTO> result = new ArrayList<>();
        if (status.equals(PostStatus.NEW)) {
            result.addAll(streams.streamAll(entityManager, Post.class)
                    .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                    .filter(p -> p.getStatus().equals(status))
                    .skip(offset)
                    .limit(limit)
                    .collect(Collectors.toList())
                    .stream()
                    .map(this::makeDTOWithAnnounceAndCommentCount)
                    .collect(Collectors.toList()));
        } else {
            result.addAll(streams.streamAll(entityManager, Post.class)
                    .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                    .filter(p -> p.getModerator() != null && p.getModerator().getId() == moderatorId)
                    .filter(p -> p.getStatus().equals(status))
                    .skip(offset)
                    .limit(limit)
                    .collect(Collectors.toList())
                    .stream()
                    .map(this::makeDTOWithAnnounceAndCommentCount)
                    .collect(Collectors.toList()));
        }
        return result;
    }

    public PostResponse getResponseMyPostsByMode(int userId, String mode) {
        PostResponse postResponse = new PostResponse();
        List<Post> posts = postRepository.findPostsByUserIdOrderByPublicationDate(userId);
        List<PostDTO> result = new ArrayList<>();

        PostStatus status;
        switch (mode) {
            case "inactive": {
                for (Post post : posts) {
                    if (!post.isPostActive()) {
                        result.add(makeDTOWithAnnounceAndCommentCount(post));
                    }
                }
                break;
            }
            case "pending": {
                status = PostStatus.NEW;
                for (Post post : posts) {
                    if (post.isPostActive() && post.getStatus() == status) {
                        result.add(makeDTOWithAnnounceAndCommentCount(post));
                    }
                }
                break;
            }
            case "declined": {
                status = PostStatus.DECLINED;
                for (Post post : posts) {
                    if (post.isPostActive() && post.getStatus().equals(status)) {
                        result.add(makeDTOWithAnnounceAndCommentCount(post));
                    }
                }
                break;
            }
            case "published": {
                status = PostStatus.ACCEPTED;
                for (Post post : posts) {
                    if (post.isPostActive() && post.getStatus() == status) {
                        result.add(makeDTOWithAnnounceAndCommentCount(post));
                    }
                }
                break;
            }
        }
        postResponse.setCount(result.size());
        postResponse.setPosts(result);
        return postResponse;
    }

    public ResponseEntity<PostDTO> getPostDTO(int id) {
        Post post = postRepository.getOne(id);
        PostDTO result;
        Optional<User> optional = userRepository.findByEmail(AuthService.getCurrentEmail());
        if (optional.isPresent()) {
            User user = optional.get();
            if (user.getId() == post.getId() || user.isModerator() || post.isPostActive()) { //ToDo status = 'ACCEPTED' if blog is premoderator
                result = makeDTOWithTagsAndComments(post, incrementViews(user, post));
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
        } else {
            postRepository.updatePostGetViewed(post.getId());
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    private UserDTO incrementViews(User user, Post post) {
        UserDTO resultUser = user.isModerator() ?
                UserDTO.makeModeratorDTO(user, countNewActiveCurrentPosts())
                : UserDTO.makeSimpleUserDTO(user);
        if (!resultUser.isModeration() & post.getUser().getId() != resultUser.getId()) {
            postRepository.updatePostGetViewed(post.getId());
        }
        return resultUser;
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
        UserDTO user = UserService.getUserDTOWithPhoto(post.getUser());
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
        List<PostVotes> postVotes = postVotesRepository.findAllByPostId(post.getId());
        for (PostVotes pv : postVotes) {
            if (pv.getValue() > 0) {
                likeCount++;
            }
            if (pv.getValue() < 0) {
                dislikeCount++;
            }
        }
        int viewCount = 0;
        if (postRepository.findById(post.getId()).isPresent()) {
            viewCount = postRepository.findById(post.getId()).get().getViewCount();
        }
        int[] result = new int[3];
        result[0] = likeCount;
        result[1] = dislikeCount;
        result[2] = viewCount;
        return result;
    }
}
