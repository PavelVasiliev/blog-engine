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
import com.model.Image;
import com.model.blog_enum.BlogError;
import com.service.CalendarService;
import com.service.CommentService;
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
public class ApiGeneralController {
    private final InitResponse initResponse;
    private final TagService tagService;
    private final SettingsService settingsService;
    private final CalendarService calendarService;
    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public ApiGeneralController(InitResponse initResponse,
                                TagService tagService,
                                SettingsService settingsService,
                                CalendarService calendarService,
                                CommentService commentService,
                                UserService userService) {
        this.initResponse = initResponse;
        this.tagService = tagService;
        this.settingsService = settingsService;
        this.calendarService = calendarService;
        this.commentService = commentService;
        this.userService = userService;
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
        CommentResponse response = new CommentResponse();
        Map<String, String> errors = checkData(request);
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
        if (Image.getMaxSize() > mf.getSize()) {
            String name = mf.getOriginalFilename();
            String path = Image.makePath("/upload/");
            Image image = new Image(name);
            return ResponseEntity.ok(image.save(mf, path));
        } else {
            DefaultResponse response = new DefaultResponse();
            Map<String, String> errors = new HashMap<>();
            errors.put(BlogError.IMAGE.name(), BlogError.IMAGE.getValue());

            response.setErrors(errors);
            return ResponseEntity.badRequest().body(response);
        }
    }

    private Map<String, String> checkData(CommentRequest request) {
        Map<String, String> result = new HashMap<>();
        if (request.getText().length() < 3) {
            result.put(BlogError.Text.name().toLowerCase(), BlogError.Text.getValue());
        }
        return result;
    }
}