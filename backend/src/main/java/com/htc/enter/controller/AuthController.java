package com.htc.enter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.htc.enter.repository.TokenRepository;
import com.htc.enter.repository.UserRepository;
import com.htc.enter.serviceimpl.JwtUtil;
import com.htc.enter.model.TokenRecord;
import com.htc.enter.model.User;
import com.htc.enter.model.Admin;
import com.htc.enter.model.Manager;
import com.htc.enter.model.Employee;
import com.htc.enter.dto.UserOutput;
import com.htc.enter.dto.AdminOutput;
import com.htc.enter.dto.ManagerOutput;
import com.htc.enter.dto.EmployeeOutput;
import com.htc.enter.mapper.UserMapper;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthController(AuthenticationManager authenticationManager, 
                         JwtUtil jwtUtil, 
                         TokenRepository tokenRepository,
                         UserRepository userRepository,
                         UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            // Generate JWT
            String token = jwtUtil.generateToken(username);

            Instant now = Instant.now();
            Instant expires = now.plusMillis(jwtUtil.getJwtExpirationMs());

            // Persist token
            tokenRepository.save(new TokenRecord(token, username, now, expires));

            // Fetch user entity and convert to DTO
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            UserOutput userOutput = toUserOutput(user);

            // Build response with token and user
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", userOutput);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing Authorization header"));
        }
        String token = authHeader.substring(7);
        return tokenRepository.findByToken(token)
                .map(rec -> {
                    rec.setRevoked(true);
                    tokenRepository.save(rec);
                    return ResponseEntity.ok(Map.of("message", "Logged out"));
                }).orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Token not found")));
    }

    private UserOutput toUserOutput(User user) {
        if (user instanceof Admin) return userMapper.toAdminOutput((Admin) user);
        if (user instanceof Manager) return userMapper.toManagerOutput((Manager) user);
        if (user instanceof Employee) return userMapper.toEmployeeOutput((Employee) user);
        return userMapper.toUserOutput(user);
    }
}