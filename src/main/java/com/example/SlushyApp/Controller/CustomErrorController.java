package com.example.SlushyApp.Controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        // Aquí puedes agregar lógica para diferentes códigos de error
        return "error/403";
    }

    public String getErrorPath() {
        return "/error";
    }

}
