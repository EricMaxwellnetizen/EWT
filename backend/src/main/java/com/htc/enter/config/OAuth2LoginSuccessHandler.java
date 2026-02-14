package com.htc.enter.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.htc.enter.model.User;
import com.htc.enter.repository.UserRepository;
import com.htc.enter.serviceimpl.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handles successful OAuth2 authentication (Google login).
 * Creates user if doesn't exist, generates JWT token, and redirects.
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        // Extract user information from Google
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");

        if (email == null || email.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not provided by Google");
            return;
        }

        // Find or create user
        User user = findOrCreateUser(email, name, googleId);

        // Generate JWT token
        String jwtToken = jwtUtil.generateToken(user.getUsername());

        // Redirect to frontend with JWT token
        // Use frontend URL directly to callback page
        String redirectUrl = String.format("http://localhost:5173/oauth2/callback?token=%s&userId=%d", jwtToken, user.getId());
        
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private User findOrCreateUser(String email, String name, String googleId) {
        // Try to find user by email
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Create new user from Google account
        User newUser = new User();
        newUser.setEmail(email);
        
        // Generate username from email (before @ symbol)
        String username = email.split("@")[0];
        
        // Check if username exists, append number if needed
        String finalUsername = username;
        int counter = 1;
        while (userRepository.findByUsername(finalUsername).isPresent()) {
            finalUsername = username + counter;
            counter++;
        }
        
        newUser.setUsername(finalUsername);
        
        // Set a random password (user won't use it, they'll login via Google)
        newUser.setPasswordhash("GOOGLE_OAUTH_" + googleId);
        
        // Set default role and access level
        newUser.setAccessLevel(1); // Regular user
        
        // Set name if available
        if (name != null && !name.isEmpty()) {
            newUser.setJob_title(name); // Store full name in job_title temporarily
        }

        return userRepository.save(newUser);
    }
}
