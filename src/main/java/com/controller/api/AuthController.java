package com.controller.api;

import com.api.request.LoginRequest;
import com.api.request.RegistrationRequest;
import com.api.request.RestorePassRequest;
import com.api.response.AuthResponse;
import com.api.response.CaptchaResponse;
import com.api.response.DefaultResponse;
import com.dto.UserDTO;
import com.model.blog_enum.BlogError;
import com.model.blog_enum.PostStatus;
import com.model.entity.User;
import com.service.AuthService;
import com.service.CaptchaService;
import com.service.PostService;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final CaptchaService captchaService;
    private final UserService userService;
    private final PostService postService;

    @Autowired
    public AuthController(AuthService authService,
                          CaptchaService captchaService,
                          UserService userService,
                          PostService postService) {
        this.authService = authService;
        this.captchaService = captchaService;
        this.userService = userService;
        this.postService = postService;
    }

    @GetMapping("/check")
    public AuthResponse check() {
        return authService.checkAuth();
    }

    @GetMapping("/captcha")
    public CaptchaResponse captcha() {
        return captchaService.getCaptchaResponse();
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = new AuthResponse();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (userService.isUserExist(loginRequest.getEmail())) {
            User user = userService.getUserByMail(loginRequest.getEmail()).orElseThrow();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                authResponse.setResult(true);
                authResponse.setUser(user.isModerator() ?
                        UserService.getModeratorDTO(user, postService
                                .countPostsToModerator(user.getId(), PostStatus.NEW)) : UserDTO.makeSimpleUserDTO(user));
                authService.authorize(user);
            }
        }
        return authResponse;
    }

    @GetMapping("/logout")
    @PreAuthorize("hasAuthority('user:write')")
    public DefaultResponse logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        DefaultResponse response = new DefaultResponse();
        if (session != null) {
            session.invalidate();
            response.setResult(true);
        }
        return response;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DefaultResponse register(@RequestBody RegistrationRequest request) {
        DefaultResponse response = new DefaultResponse();

        Map<String, String> errors = checkData(request);
        if (errors.isEmpty()) {
            response.setResult(true);
            userService.saveUser(
                    User.makeSimpleUser(
                            request.getName(),
                            request.getEmail(),
                            new BCryptPasswordEncoder().encode(request.getPassword())));
        } else {
            response.setErrors(errors);
        }
        return response;
    }

    @PostMapping("/restore")
    public DefaultResponse sendRestoreCode(@RequestBody RestorePassRequest request) {
        return authService.sendRestoreCode(request.getEmail());
    }

    @PostMapping("/password")
    public DefaultResponse restore(@RequestBody RestorePassRequest request) {
        DefaultResponse response = new DefaultResponse();
        Map<String, String> errors = checkData(request);
        if (errors.isEmpty()) {
            response.setResult(true);
            authService.changePassword(request.getCode(), request.getPassword());
        } else {
            response.setErrors(errors);
        }
        return response;
    }

    private Map<String, String> checkData(RegistrationRequest request) {
        Map<String, String> result = new HashMap<>();
        if (!captchaService.findCodeBySecret(request.getCaptchaSecret()).equals(request.getCaptcha())) {
            result.put(BlogError.CAPTCHA.name().toLowerCase(), BlogError.CAPTCHA.getValue());
        }
        if (userService.isUserExist(request.getEmail()) || !UserService.validateMail(request.getEmail())) {
            result.put(BlogError.EMAIL.name().toLowerCase(), BlogError.EMAIL.getValue());
        }
        if (!UserService.validateName(request.getName())) {
            result.put(BlogError.NAME.name().toLowerCase(), BlogError.NAME.getValue());
        }
        if (request.getPassword() != null) {
            if (!UserService.validatePassword((byte) request.getPassword().length())) {
                result.put(BlogError.PASSWORD.name().toLowerCase(), BlogError.PASSWORD.getValue());
            }
        }
        return result;
    }

    private Map<String, String> checkData(RestorePassRequest request) {
        Map<String, String> result = new HashMap<>();
        if (!userService.isCodeRight("%" + request.getCode())) {
            result.put(BlogError.CODE.name().toLowerCase(), BlogError.CODE.getValue());
        }
        if (!UserService.validatePassword((byte) request.getPassword().length())) {
            result.put(BlogError.PASSWORD.name().toLowerCase(), BlogError.PASSWORD.getValue());
        }
        if (!captchaService.findCodeBySecret(request.getCaptchaSecret()).equals(request.getCaptcha())) {
            result.put(BlogError.CAPTCHA.name().toLowerCase(), BlogError.CAPTCHA.getValue());
        }
        return result;
    }
}
