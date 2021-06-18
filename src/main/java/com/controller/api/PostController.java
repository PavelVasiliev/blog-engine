package com.controller.api;

import com.api.request.PostRequest;
import com.api.request.VoteRequest;
import com.api.response.DefaultResponse;
import com.api.response.PostResponse;
import com.dto.PostDTO;
import com.model.blog_enum.BlogError;
import com.service.AuthService;
import com.service.PostService;
import com.service.UserService;
import com.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/post")
public class PostController {

    private final String DEFAULT_OFFSET = "0";
    private final String DEFAULT_LIMIT = "10";
    private final String DEFAULT_MODE = "recent";

    private final PostService postService;
    private final VoteService voteService;
    private final UserService userService;

    @Autowired
    public PostController(PostService postService, VoteService voteService, UserService userService) {
        this.postService = postService;
        this.voteService = voteService;
        this.userService = userService;
    }

    @GetMapping("")
    @ResponseBody
    public PostResponse getResponseByMode(@RequestParam(defaultValue = DEFAULT_OFFSET) String offset,
                                          @RequestParam(defaultValue = DEFAULT_LIMIT) String limit,
                                          @RequestParam(defaultValue = DEFAULT_MODE) String mode) {
        return postService.getResponseByModerationStatus(offset, limit, mode);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(@PathVariable int id) {
        return postService.getPostDTO(id);
    }

    @GetMapping("/my")
    @ResponseBody
    public ResponseEntity<PostResponse> my(@RequestParam(defaultValue = DEFAULT_OFFSET) String offset,
                                           @RequestParam(defaultValue = DEFAULT_LIMIT) String limit,
                                           @RequestParam String status) {
        int userId = userService.getUserByMail(AuthService.getCurrentEmail()).orElseThrow().getId();
        return ResponseEntity.ok(postService.getResponseMyPostsByMode(userId, status, offset, limit));
    }

    @GetMapping("/moderation")
    @PreAuthorize("hasAuthority('moderator:moderate')")
    public ResponseEntity<PostResponse> moderation(@RequestParam(defaultValue = DEFAULT_OFFSET) String offset,
                                                   @RequestParam(defaultValue = DEFAULT_LIMIT) String limit,
                                                   @RequestParam String status) {

        int id = userService.getUserByMail(AuthService.getCurrentEmail()).orElseThrow().getId();
        return ResponseEntity.ok().body(postService.getPostsToModerate(id, offset, limit, status));
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<PostResponse> search(@RequestParam(defaultValue = DEFAULT_OFFSET) String offset,
                                               @RequestParam(defaultValue = DEFAULT_LIMIT) String limit,
                                               @RequestParam String query) {
        if (query.replaceAll("\\s+", "").trim().equals("")) {
            return ResponseEntity.status(HttpStatus.OK).body(getResponseByMode(offset, limit, DEFAULT_MODE));
        }
        return postService.getResponseByQuery(offset, limit, query);
    }

    @GetMapping("/byDate")
    @ResponseBody
    public PostResponse byDate(@RequestParam(defaultValue = DEFAULT_OFFSET) String offset,
                               @RequestParam(defaultValue = DEFAULT_LIMIT) String limit,
                               @RequestParam String date) {
        return postService.getResponseByDate(offset, limit, date);
    }

    @GetMapping("/byTag")
    @ResponseBody
    public PostResponse byTag(@RequestParam(defaultValue = DEFAULT_OFFSET) String offset,
                              @RequestParam(defaultValue = DEFAULT_LIMIT) String limit,
                              @RequestParam String tag) {
        return postService.getResponseByTag(offset, limit, tag);
    }

    @PostMapping("")
    @ResponseBody
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<DefaultResponse> post(@RequestBody PostRequest request) {
        DefaultResponse response = new DefaultResponse();
        Map<String, String> errors = checkData(request);
        if (!errors.isEmpty()) {
            response.setErrors(errors);
        } else {
            response.setResult(true);
            postService.post(request, checkDate(request.getTimestamp()));
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/like")
    @PreAuthorize("hasAuthority('user:write')")
    public DefaultResponse like(@RequestBody VoteRequest request) {
        DefaultResponse response = new DefaultResponse();
        response.setResult(voteService.vote((byte) 1, request));
        return response;
    }

    @PostMapping("/dislike")
    @PreAuthorize("hasAuthority('user:write')")
    public DefaultResponse dislike(@RequestBody VoteRequest request) {
        DefaultResponse response = new DefaultResponse();
        response.setResult(voteService.vote((byte) -1, request));
        return response;
    }

    @PutMapping("/{id}")
    public ResponseEntity<DefaultResponse> editPost(@PathVariable int id,
                                                    @RequestBody PostRequest request) {
        DefaultResponse response = new DefaultResponse();
        Map<String, String> errors = checkData(request);

        if (!errors.isEmpty()) {
            response.setErrors(errors);
        } else {
            response.setResult(true);
            postService.editPost(id, request, checkDate(request.getTimestamp()));
        }
        return ResponseEntity.ok(response);
    }

    private Map<String, String> checkData(PostRequest request) {
        Map<String, String> result = new HashMap<>();
        if (request.getTitle().trim().length() < PostService.MIN_TITLE_LENGTH) {
            result.put(BlogError.TITLE.name().toLowerCase(), BlogError.TITLE.getDescription());
        }
        if (request.getText().trim().length() < PostService.MIN_TEXT_LENGTH) {
            result.put(BlogError.TEXT.name().toLowerCase(), BlogError.TEXT.getDescription());
        }
        return result;
    }

    private long checkDate(long requestTimestamp) {
        Date current = new Date();
        return current.after(new Date(requestTimestamp * 1000)) ? (current.getTime() / 1000) : requestTimestamp;
    }
}
