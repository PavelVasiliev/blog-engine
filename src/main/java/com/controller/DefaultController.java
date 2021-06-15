package com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DefaultController {

    @GetMapping("/")
    public String getMainPage() {
        return "index";
    }

    @RequestMapping(method = {RequestMethod.OPTIONS,
            RequestMethod.GET}, value = "/**/{path:[^\\\\.]*}")
    public String redirectToIndex() {
        return "forward:/";
    }
}