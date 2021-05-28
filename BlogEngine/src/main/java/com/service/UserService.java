package com.service;

import com.api.request.ModifyUserRequest;
import com.api.response.DefaultResponse;
import com.dto.UserDTO;
import com.model.Image;
import com.model.blog_enum.PostStatus;
import com.model.entity.Post;
import com.model.entity.User;
import com.repo.PostRepository;
import com.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService {
    private static final Pattern PATTERN_NAME =
            Pattern.compile("^[A-Z][a-z]{0,30}|[А-ЯЁ][а-яё]{0,30}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_EMAIL =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final byte MIN_PASSWORD_LENGTH = 6;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Autowired
    public UserService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public static boolean validateName(String name) {
        Matcher matcher = PATTERN_NAME.matcher(name);
        return matcher.find();
    }

    public static boolean validateMail(String email) {
        Matcher matcher = PATTERN_EMAIL.matcher(email);
        return matcher.find();
    }

    public static boolean validatePassword(byte length) {
        return length >= MIN_PASSWORD_LENGTH;
    }

    public static UserDTO getUserDTO(User user) {
        return UserDTO.makeSimpleUserDTO(user);
    }

    public static UserDTO getModeratorDTO(User user, int moderatingCount) {
        return UserDTO.makeModeratorDTO(user, moderatingCount);
    }

    public static UserDTO getUserDTOWithPhoto(User user) {
        return UserDTO.makeUserDTOWithPhoto(user);
    }

    public boolean isUserExist(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean isCodeRight(String code) {
        return userRepository.findByCodeIsLike(code) != null;
    }

    public void saveUser(User user) {
        if (!isUserExist(user.getEmail())) {
            userRepository.save(user);
        }
    }

    public User getUserByMail(String mail) {
        return userRepository.findByEmail(mail).isPresent() ? userRepository.findByEmail(mail).get() : new User();
    }

    public void editUserProfile(ModifyUserRequest request, User user) {
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPassword() != null) {
            if (request.getPassword().length() >= UserService.MIN_PASSWORD_LENGTH)
                user.setPassword(new BCryptPasswordEncoder().encode(request.getPassword()));
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhoto() != null) {
            String path = Image.makePath("/avatars/");
            String name = request.getPhoto().getOriginalFilename();
            user.setPhoto(path + name);

            Image avatar = new Image(name);
            avatar.save(request.getPhoto(), path);
        }
        if (request.getRemovePhoto() == (byte) 1) {
            user.setPhoto("");
        }
        userRepository.save(user);
    }

    public DefaultResponse moderate(int postId, String decision) {
        DefaultResponse response = new DefaultResponse();
        Optional<User> optional = userRepository.findByEmail(AuthService.getCurrentEmail());
        if (optional.isPresent()) {
            Post post = postRepository.getOne(postId);
            User moderator = optional.get();
            PostStatus status;
            if (decision.equalsIgnoreCase("accept")) {
                status = PostStatus.ACCEPTED;
            } else {
                status = PostStatus.DECLINED;
            }
            moderator.moderate(post, status);
            postRepository.save(post);
            response.setResult(true);
            return response;
        }
        return response;
    }
}
