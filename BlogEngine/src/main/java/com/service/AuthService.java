package com.service;

import com.api.response.AuthResponse;
import com.api.response.DefaultResponse;
import com.dto.UserDTO;
import com.model.blog_enum.PostStatus;
import com.model.entity.User;
import com.repo.PostRepository;
import com.repo.UserRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    @Getter
    private final String RESTORE_LINK = "/login/change-password/";

    @Autowired
    public AuthService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    //ToDo JWT?
    public AuthResponse authorize(User user) {
        AuthResponse authResponse = new AuthResponse();
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        if (user.isModerator()) {
            int postsModerate = postRepository.findPostsByStatus(PostStatus.NEW).size();
            authResponse.setUser(UserDTO.makeModeratorDTO(user, postsModerate));
        } else {
            authResponse.setUser(UserDTO.makeSimpleUserDTO(user));
        }
        return authResponse;
    }

    public AuthResponse checkAuth() {
        AuthResponse authResponse = new AuthResponse();
        Optional<User> optional = userRepository.findByEmail(getCurrentEmail());
        if (optional.isPresent()) {
            authResponse.setResult(true);
            User user = optional.get();
            if (user.isModerator()) {
                authResponse.setUser(UserService.
                        getModeratorDTO(
                                user,
                                postRepository.findPostsByStatus(PostStatus.NEW).size()));
            } else {
                authResponse.setUser(UserService.getUserDTO(user));
            }
        }
        return authResponse;
    }

    public static String getCurrentEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String mail;
        if (principal instanceof UserDetails) {
            mail = ((UserDetails) principal).getUsername();
        } else {
            mail = principal.toString();
        }
        return mail;
    }

    public DefaultResponse sendRestoreCode(String email) {
        DefaultResponse response = new DefaultResponse();
        Optional<User> optional = userRepository.findByEmail(email);
        if (optional.isPresent()) {
            response.setResult(true);
            User user = optional.get();
            user.setCode(generateCodeRestore(email));
            userRepository.save(user);
        }
        return response;
    }

    private String generateCodeRestore(String email) {
        String hash = new BCryptPasswordEncoder().encode(email + new Date());
        hash = hash.replaceAll("[</.*$?>]", "");
        return RESTORE_LINK + hash;
    }

    public void changePassword(String code, String password) {
        User user = userRepository.findByCodeIsLike("%" + code);
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        userRepository.save(user);
    }
}