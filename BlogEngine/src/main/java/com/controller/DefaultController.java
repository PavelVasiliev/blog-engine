package com.controller;

import com.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DefaultController {

    private final PostService postService;

    @Autowired
    public DefaultController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/")
    public String getMainPage() {
        return "index";
    }

    //ToDo correct redirect
    @GetMapping("/posts/{mode}")
    public String getPosts(@PathVariable String mode, Model model) {
        postService.getResponseByModerationStatus(mode);
        model.addAttribute(postService);
        return "redirect:/";
    }

    @RequestMapping(method = {RequestMethod.OPTIONS,
            RequestMethod.GET}, value = "/**/{path:[^\\\\.]*}")
    public String redirectToIndex() {
        return "forward:/";
    }
}