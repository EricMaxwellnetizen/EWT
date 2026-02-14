package com.htc.enter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class FrontendRedirectController {

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @GetMapping("/login")
    public void redirectLogin(HttpServletResponse response) {
        response.setStatus(HttpStatus.FOUND.value());
        response.setHeader("Location", frontendBaseUrl + "/login");
    }
}
