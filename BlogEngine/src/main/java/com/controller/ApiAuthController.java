package com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiAuthController {
    private final static String PATH = "/api/auth/";

    @GetMapping(PATH)
    public String authorize(Model model) {
        System.out.println("authorized");
        return "";
    }
}
