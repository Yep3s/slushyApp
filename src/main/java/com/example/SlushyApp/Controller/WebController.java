package com.example.SlushyApp.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class WebController {

    @GetMapping()
    public String mostrarIndex() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/logoutSlushy")
    public String logout() {
        return "logout";
    }

    @GetMapping("/registerSlushy")
    public String registerSlushy() {
        return "register";
    }


}
