package com.example.SlushyApp.Controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class WebControllerAdmin {

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "admin/adminDashboard";
    }


}
