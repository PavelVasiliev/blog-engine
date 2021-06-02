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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public static List<CommentDTO> makeDTOList(List<Comment> comments) {
        List<CommentDTO> result = new ArrayList<>();
        for (Comment c : comments) {
            result.add(makeDTO(c));
        }
        return result;
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
                Comment comment =
                        new Comment(
                                user.get(),
                                post,
                                parent,
                                request.getText(),
                                commentRepository.findAll().size() + 1); //ToDo smthg with id Identity
                response.setId(comment.getId());
                commentRepository.save(comment);
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.badRequest().body(response);
    }
}
