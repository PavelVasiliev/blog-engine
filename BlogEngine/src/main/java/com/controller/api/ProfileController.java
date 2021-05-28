package com.controller.api;

import com.api.request.ModifyUserRequest;
import com.api.response.DefaultResponse;
import com.model.Image;
import com.model.MultipartFileImpl;
import com.model.blog_enum.BlogError;
import com.model.entity.User;
import com.service.AuthService;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;

    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/my", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('user:write')")
    public DefaultResponse modifyProfilePhoto(@RequestParam(value = "photo") @NotNull MultipartFile photo,
                                              @RequestParam("email") String email,
                                              @RequestParam("name") String name,
                                              @RequestParam(value = "password", required = false) String password,
                                              @RequestParam("removePhoto") byte removePhoto) {
        ModifyUserRequest req = new ModifyUserRequest();
        req.setPhoto(new MultipartFileImpl(photo));
        req.setEmail(email);
        req.setPassword(password);
        req.setRemovePhoto(removePhoto);
        req.setName(name);
        return modifyProfile(req);
    }

    @PostMapping("/my")
    @PreAuthorize("hasAuthority('user:write')")
    public DefaultResponse modifyProfile(@RequestBody ModifyUserRequest request) {
        if (request.getRemovePhoto() == 1) {
            request.setPhoto(null);
        }
        DefaultResponse response = new DefaultResponse();
        Map<String, String> errors = checkData(request);
        if (errors.isEmpty()) {
            response.setResult(true);
            String mail = AuthService.getCurrentEmail();
            User user = userService.getUserByMail(mail);
            userService.editUserProfile(request, user);
        } else {
            response.setErrors(errors);
        }
        return response;
    }

    private Map<String, String> checkData(ModifyUserRequest request) {
        Map<String, String> result = new HashMap<>();
        if (request.getEmail() != null) {
            if (!UserService.validateMail(request.getEmail())
                    || userService.isUserExist(request.getEmail())
                    & !AuthService.getCurrentEmail().equals(request.getEmail())) {
                result.put(BlogError.EMAIL.name().toLowerCase(), BlogError.EMAIL.getValue());
            }
        }
        if (request.getPhoto() != null) {
            if (request.getPhoto().getSize() > Image.getMaxSize()) {
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
