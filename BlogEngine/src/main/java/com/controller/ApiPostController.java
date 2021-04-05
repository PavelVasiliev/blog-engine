package com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiPostController {
    private final static String PATH = "/api/post/";

    @GetMapping(PATH)
    public String post(Model model) {
        System.out.println("Posted");
        return "";
    }
}
