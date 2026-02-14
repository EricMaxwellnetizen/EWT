package com.htc.enter.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to handle OAuth2 redirects and provide token to frontend.
 */
@RestController
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @GetMapping("/redirect")
    public ResponseEntity<Map<String, String>> oauth2Redirect(
            @RequestParam String token,
            @RequestParam String username) {
        
        return ResponseEntity.ok(Map.of(
            "token", token,
            "username", username,
            "message", "Login successful! Use this token for API requests."
        ));
    }
}
