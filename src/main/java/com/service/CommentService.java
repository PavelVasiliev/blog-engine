package com.service;

import com.api.request.CommentRequest;
import com.api.response.CommentResponse;
import com.dto.CommentDTO;
import com.dto.UserDTO;
import com.model.entity.Comment;
import com.model.entity.Post;
import com.model.entity.User;
import com.repo.CommentRepository;
import com.repo.PostRepository;
import com.repo.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LogManager.getLogger(CommentService.class);

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public static List<CommentDTO> makeDTOList(List<Comment> comments) {
        return comments.stream().map(CommentService::makeDTO).collect(Collectors.toList());
    }

    private static CommentDTO makeDTO(Comment comment) {
        UserDTO user = UserService.getUserDTOWithPhoto(comment.getUser());
        return new CommentDTO(
                comment.getId(),
                comment.getTime(),
                comment.getText(),
                user);
    }

    public ResponseEntity<CommentResponse> comment(CommentRequest request) {
        CommentResponse response = new CommentResponse();
        Comment parent = null;
        if (request.getParentId() != null) {
            int parentId = Integer.parseInt(request.getParentId());
            if (commentRepository.existsById(parentId)) {
                parent = commentRepository.getOne(parentId);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        }
        int postId = Integer.parseInt(request.getPostId());
        if (postRepository.existsById(postId)) {
            Optional<User> user = userRepository.findByEmail(AuthService.getCurrentEmail());
            if (user.isPresent()) {
                Post post = postRepository.getOne(postId);
                Comment comment = Comment.makeComment(
                        user.get(),
                        post,
                        parent,
                        request.getText());
                response.setId(comment.getId());
                commentRepository.save(comment);
                logger.info(post.getUser().getEmail() + " left a comment to post " + post.getId());
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.badRequest().body(response);
    }

    public Optional<Comment> findById(String id) {
        return commentRepository.findById(Integer.parseInt(id));
    }
}