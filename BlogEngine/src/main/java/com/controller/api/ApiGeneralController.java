package com.controller.api;

import com.api.request.CommentRequest;
import com.api.request.ModeratePostRequest;
import com.api.request.ModifyUserRequest;
import com.api.request.SettingsRequest;
import com.api.response.CalendarResponse;
import com.api.response.CommentResponse;
import com.api.response.InitResponse;
import com.api.response.ModeratePostResponse;
import com.api.response.ModificationResponse;
import com.api.response.SettingsResponse;
import com.api.response.StatisticsResponse;
import com.api.response.TagResponse;
import com.model.Image;
import com.model.MultipartFileImpl;
import com.model.blog_enum.BlogError;
import com.model.entity.User;
import com.service.AuthService;
import com.service.CalendarService;
import com.service.CommentService;
import com.service.SettingsService;
import com.service.StatisticsService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
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
    private final StatisticsService statisticsService;
    private final UserService userService;

    @Autowired
    public ApiGeneralController(InitResponse initResponse,
                                TagService tagService,
                                SettingsService settingsService,
                                CalendarService calendarService,
                                CommentService commentService,
                                StatisticsService statisticsService,
                                UserService userService) {
        this.initResponse = initResponse;
        this.tagService = tagService;
        this.settingsService = settingsService;
        this.calendarService = calendarService;
        this.commentService = commentService;
        this.statisticsService = statisticsService;
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

    @GetMapping("/statistics/my")
    @PreAuthorize("hasAuthority('user:write')")
    public StatisticsResponse statisticsMy() {
        return statisticsService.getMyStats();
    }

    @GetMapping("statistics/all")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<StatisticsResponse> statisticsAll() {
        return statisticsService.getAllStats();
    }

    private Map<String, String> checkData(CommentRequest request) {
        Map<String, String> result = new HashMap<>();
        if (request.getText().length() < 3) {
            result.put(BlogError.Text.name().toLowerCase(), BlogError.Text.getValue());
        }
        return result;
    }

    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('moderator:moderate')")
    public void changeBlogSettings(@RequestBody SettingsRequest request) {
        settingsService.save(request);
    }

    @PostMapping("/moderation")
    @PreAuthorize("hasAuthority('moderator:moderate')")
    public ModeratePostResponse moderatePost(@RequestBody ModeratePostRequest request) {
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
    public String addImage(@RequestParam("image") MultipartFile mf) {
        String name = mf.getOriginalFilename();
        String path = Image.makePath("/upload/");
        Image image = new Image(name);
        return image.save(mf, path);
    }


    //ToDo f* it
    @PostMapping(value = "/profile/my", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('user:write')")
    public ModificationResponse modifyProfilePhoto(@RequestParam(value = "photo") @NotNull MultipartFile photo,
                                                   @RequestParam("email") String email,
                                                   @RequestParam("name") String name,
                                                   @RequestParam(value = "password", required = false) String password,
                                                   @RequestParam("removePhoto") byte removePhoto) {
        ModifyUserRequest req = new ModifyUserRequest();
        req.setEmail(email);
        req.setPassword(password);
        req.setRemovePhoto(removePhoto);
        req.setName(name);
        return modifyProfile(req, photo);
    }


    @PostMapping("/profile/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ModificationResponse modifyProfile(@RequestBody ModifyUserRequest request, MultipartFile mf) {
        MultipartFileImpl photo = new MultipartFileImpl();
        if(mf != null){
            photo = new MultipartFileImpl(mf);
        } else {
            request.setPhoto(null);
        }
        ModificationResponse response = new ModificationResponse();
        Map<String, String> errors = checkData(request, photo);
        if (errors.isEmpty()) {
            response.setResult(true);
            String mail = AuthService.getCurrentEmail();
            User user = userService.getUserByMail(mail);
            userService.editUserProfile(request, user, photo);
        } else {
            response.setErrors(errors);
        }
        return response;
    }

    private Map<String, String> checkData(ModifyUserRequest request, MultipartFile mf) {
        Map<String, String> result = new HashMap<>();
        if (request.getEmail() != null) {
            if (!UserService.validateMail(request.getEmail()) ||
                    userService.isUserExist(request.getEmail()) & !AuthService.getCurrentEmail().equals(request.getEmail())) {
                result.put(BlogError.EMAIL.name().toLowerCase(), BlogError.EMAIL.getValue());
            }
        }
        if (mf != null) {
            if (mf.getSize() > Image.MAX_SIZE) {
                result.put(BlogError.PHOTO.name().toLowerCase(), BlogError.PHOTO.getValue());
            }
        }
        if (request.getName() != null) {
            if (!UserService.validateName(request.getName())) {
                result.put(BlogError.NAME.name().toLowerCase(), BlogError.NAME.getValue());
            }
        }
        if (request.getPassword() != null) {
            if (!UserService.validatePassword((byte) request.getPassword().length())) {
                result.put(BlogError.PASSWORD.name().toLowerCase(), BlogError.PASSWORD.getValue());
            }
        }
        return result;
    }
}
