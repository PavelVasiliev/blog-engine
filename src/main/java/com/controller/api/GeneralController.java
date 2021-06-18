package com.controller.api;

import com.api.request.CommentRequest;
import com.api.request.ModeratePostRequest;
import com.api.request.SettingsRequest;
import com.api.response.CalendarResponse;
import com.api.response.CommentResponse;
import com.api.response.DefaultResponse;
import com.api.response.InitResponse;
import com.api.response.SettingsResponse;
import com.api.response.TagResponse;
import com.model.blog_enum.BlogError;
import com.model.blog_enum.ImageExtension;
import com.model.image.BlogImage;
import com.service.BlogImageService;
import com.service.CalendarService;
import com.service.CommentService;
import com.service.PostService;
import com.service.SettingsService;
import com.service.TagService;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GeneralController {
    private static final String PATH_IMAGE = "/upload/";
    private final InitResponse initResponse;
    private final TagService tagService;
    private final SettingsService settingsService;
    private final CalendarService calendarService;
    private final CommentService commentService;
    private final UserService userService;
    private final PostService postService;

    @Autowired
    public GeneralController(InitResponse initResponse,
                             TagService tagService,
                             SettingsService settingsService,
                             CalendarService calendarService,
                             CommentService commentService,
                             UserService userService,
                             PostService postService) {
        this.initResponse = initResponse;
        this.tagService = tagService;
        this.settingsService = settingsService;
        this.calendarService = calendarService;
        this.commentService = commentService;
        this.userService = userService;
        this.postService = postService;
    }

    @GetMapping("/init")
    public InitResponse init() {
        return initResponse;
    }

    @GetMapping("/settings")
    public SettingsResponse getBlogSettings() {
        return settingsService.getBlogSettings();
    }

    @GetMapping("/tag")
    public TagResponse tag(@RequestParam(defaultValue = "") String query) {
        return tagService.getTagWeight(query);
    }

    @GetMapping("/calendar")
    public CalendarResponse calendar(@RequestParam String year) {
        return calendarService.getCalendarResponse(year);
    }

    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('moderator:moderate')")
    public void changeBlogSettings(@RequestBody SettingsRequest request) {
        settingsService.save(request);
    }

    @PostMapping("/moderation")
    @PreAuthorize("hasAuthority('moderator:moderate')")
    public DefaultResponse moderatePost(@RequestBody ModeratePostRequest request) {
        return userService.moderate(request.getPostId(), request.getDecision());
    }

    @PostMapping("/comment")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<CommentResponse> comment(@RequestBody CommentRequest request) {
        if (!checkData(request)) {
            return ResponseEntity.badRequest().body(new CommentResponse());
        }

        CommentResponse response = new CommentResponse();
        Map<String, String> errors = checkText(request);
        if (errors.isEmpty()) {
            return commentService.comment(request);
        } else {
            response.setErrors(errors);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> addImage(@RequestParam("image") MultipartFile mf) {
        String name = mf.getOriginalFilename();
        assert name != null;
        if (BlogImage.MAX_SIZE > mf.getSize()
                &&
                (name.endsWith(ImageExtension.JPG.getExtension()) || name.endsWith(ImageExtension.PNG.getExtension()))) {
            return ResponseEntity.ok(BlogImageService.save(mf, PATH_IMAGE, false));
        } else {
            DefaultResponse response = new DefaultResponse();
            Map<String, String> errors = new HashMap<>();
            errors.put(BlogError.IMAGE.name(), BlogError.IMAGE.getDescription());

            response.setErrors(errors);
            return ResponseEntity.badRequest().body(response);
        }
    }

    private Map<String, String> checkText(CommentRequest request) {
        Map<String, String> result = new HashMap<>();
        if (request.getText().length() < 3) {
            result.put(BlogError.Text.name().toLowerCase(), BlogError.Text.getDescription());
        }
        return result;
    }

    private boolean checkData(CommentRequest request) {
        return request.getParentId() != null ? commentService.findById(request.getParentId()).isPresent()
                || postService.findById(Integer.parseInt(request.getPostId())).isPresent()
                : postService.findById(Integer.parseInt(request.getPostId())).isPresent();
    }
}
