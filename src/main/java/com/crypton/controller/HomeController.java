package com.crypton.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

@Controller
public class HomeController {

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        model.addAttribute("appName", "Crypton");
        model.addAttribute("version", "0.0.1-SNAPSHOT");
        model.addAttribute("javaVersion", System.getProperty("java.version"));
        model.addAttribute("serverTime", LocalDateTime.now());
        model.addAttribute("status", "Running Successfully");
        return "index";
    }
}
