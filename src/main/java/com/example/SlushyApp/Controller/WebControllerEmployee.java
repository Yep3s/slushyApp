package com.example.SlushyApp.Controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employee")
public class WebControllerEmployee {

    @GetMapping("/dashboard")
    public String employeeDashboard() {
        return "employee/employeeDashboard";
    }


}
