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
import org.jinq.orm.stream.JinqStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository,
                       PostVotesRepository postVotesRepository,
                       CommentRepository commentRepository,
                       Tag2PostService tag2PostService, TagRepository tagRepository, EntityManager entityManager) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postVotesRepository = postVotesRepository;
        this.commentRepository = commentRepository;
        this.tag2PostService = tag2PostService;
        this.tagRepository = tagRepository;
        this.entityManager = entityManager;
    }

    public List<Post> getPostsByUserId(int id) {
        return postRepository.findPostsByUserIdOrderByPublicationDate(id);
    }

    public int countNewPosts() {
        return postRepository.findPostsByStatus(PostStatus.NEW).size();
    }

    public List<Post> getActivePosts() {
        return postRepository.findPostsByIsActiveEqualsAndPublicationDateLessThan((byte) 1, new Date());
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
        int sizeActive = postRepository.findActiveCurrent().size();

        PostStatus moderationStatus = PostStatus.valueOf(postMode.toUpperCase());
        switch (moderationStatus) {
            case RECENT: {
                List<Post> recent = postRepository.findRecent(offset, limit);
                postResponse.setCount(postRepository.findRecent(0, sizeActive).size());
                postResponse.setPosts(getPostsDTOs(recent));
                return postResponse;
            }
            case EARLY: {
                List<Post> early = postRepository.findOld(offset, limit);
                postResponse.setCount(postRepository.findOld(0, sizeActive).size());
                postResponse.setPosts(getPostsDTOs(early));
                return postResponse;
            }
            case BEST: {
                List<Post> best = postRepository.findBest(offset, limit);
                postResponse.setCount(postRepository.findBest(0, sizeActive).size());
                postResponse.setPosts(getPostsDTOs(best));
                return postResponse;
            }
            case POPULAR: {
                List<Post> popular = postRepository.findPopular(offset, limit);
                postResponse.setCount(postRepository.findPopular(0, sizeActive).size());
                postResponse.setPosts(getPostsDTOs(popular));
                return postResponse;
            }
        }
        return postResponse;
    }

    public ResponseEntity<PostResponse> getResponseByQuery(String offsetString, String limitString, String query) {
        PostResponse postResponse = new PostResponse();
        int offset = Integer.parseInt(offsetString);
        int limit = Integer.parseInt(limitString) + offset;
        int size = postRepository.findByQuery(query, 0, postRepository.findActiveCurrent().size()).size();

        postResponse.setCount(size);
        List<PostDTO> result = new ArrayList<>();
        List<Post> posts = postRepository.findByQuery(query, offset, limit);
        for (Post post : posts) {
            result.add(makeDTOWithAnnounceAndCommentCount(post));
        }
        postResponse.setPosts(result);
        return ResponseEntity.status(HttpStatus.OK).body(postResponse);
    }

    public List<PostDTO> getPostsByYear(int year) {
        return getPostsDTOs(postRepository.findByYear(String.valueOf(year)));
    }

    public PostResponse getResponseByDate(String offsetString, String limitString, String date) {
        PostResponse postResponse = new PostResponse();
        int offset = Integer.parseInt(offsetString);
        int limit = Integer.parseInt(limitString) + offset;
        int size = postRepository.findByDate(0, postRepository.findActiveCurrent().size(), date).size();
        postResponse.setCount(size);
        postResponse.setPosts(getPostsDTOs(postRepository.findByDate(offset, limit, date)));
        return postResponse;
    }

    public PostResponse getResponseByTag(String offsetString, String limitString, String tag) {
        PostResponse postResponse = new PostResponse();
        int offset = Integer.parseInt(offsetString);
        int limit = Integer.parseInt(limitString) + offset;
        int size = postRepository.findByTag(0, postRepository.findActiveCurrent().size(), tag).size();
        postResponse.setCount(size);
        postResponse.setPosts(getPostsDTOs(postRepository.findByTag(offset, limit, tag)));
        return postResponse;
    }

    public PostResponse getPostsToModerate(int moderatorId, String offset, String limit, String moderationStatus) {
        PostResponse response = new PostResponse();

        List<PostDTO> result = new ArrayList<>();
        PostStatus status;
        switch (moderationStatus) {
            case "new": {
                status = PostStatus.NEW;
                List<Post> posts = postRepository.findPostsByStatus(status);
                for (Post post : posts) {
                    if (post.isPostActive()) {
                        result.add(makeDTOWithAnnounceAndCommentCount(post));
                    }
                }
                break;
            }
            case "accepted": {
                status = PostStatus.ACCEPTED;
                List<Post> posts = postRepository.findPostsByModeratorIdAndStatus(moderatorId, status);
                for (Post post : posts) {
                    if (post.isPostActive()) {
                        result.add(makeDTOWithAnnounceAndCommentCount(post));
                    }
                }
                break;
            }
            case "declined": {
                status = PostStatus.DECLINED;
                List<Post> posts = postRepository.findPostsByModeratorIdAndStatus(moderatorId, status);
                for (Post post : posts) {
                    if (post.isPostActive()) {
                        result.add(makeDTOWithAnnounceAndCommentCount(post));
                    }
                }
                break;
            }
        }
        response.setPosts(result);
        response.setCount(result.size());
        return response;
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

        //FIXME its fg working

        /*
   stream()
  .skip(10)
  .limit(20)
  .toList()
And the generated SQL is:
select c.* from car c limit ? offset ?
*/
        JinqJPAStreamProvider streams =
                new JinqJPAStreamProvider(entityManager.getMetamodel());
        JinqStream<Post> posts =
                streams.streamAll(entityManager, Post.class);
        for(Post p : posts.toList()){
            System.out.println(p.toString());
        }


        Post post = postRepository.getOne(id);
        PostDTO result;
        Optional<User> optional = userRepository.findByEmail(AuthService.getCurrentEmail());
        if (optional.isPresent()) {
            User user = optional.get();
            if (user.getId() == post.getId() || user.isModerator() || post.isPostActive()) { //ToDo status = 'ACCEPTED' if blog is premoderator
                result = makeDTOWithTagsAndComments(post, incrementViews(optional, post));
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    private UserDTO incrementViews(Optional<User> user, Post post) {
        UserDTO resultUser = new UserDTO();
        if (user.isPresent()) {
            User u = user.get();
            resultUser = u.isModerator() ?
                    UserDTO.makeModeratorDTO(u, postRepository.findPostsByStatus(PostStatus.NEW).size())
                    : UserDTO.makeSimpleUserDTO(u);
            if (!resultUser.isModeration() & post.getUser().getId() != resultUser.getId()) {
                postRepository.updatePostGetViewed(post.getId());
            }
        } else {
            postRepository.updatePostGetViewed(post.getId());
        }
        return resultUser;
    }

    private List<PostDTO> getPostsDTOs(List<Post> posts) {
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
