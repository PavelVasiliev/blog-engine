package com.controller;

import com.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DefaultController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/")
    public String getMainPage(Model model) {
        return "index";
    }
}
