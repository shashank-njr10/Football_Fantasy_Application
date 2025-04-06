package com.example.fantasy_football_backend.controllers;

import com.example.fantasy_football_backend.dto.UserDTO;
import com.example.fantasy_football_backend.models.User;
import com.example.fantasy_football_backend.security.JwtTokenUtil;
import com.example.fantasy_football_backend.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthController(UserService userService, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        UserDTO newUser = userService.createUser(user);

        // Generate token
        String token = jwtTokenUtil.generateToken(user.getEmail(), user.isAdmin());

        Map<String, Object> response = new HashMap<>();
        response.put("user", newUser);
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        // Token is validated by JwtRequestFilter, if we get here, it's valid
        // Extract token from header
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String username = jwtTokenUtil.extractUsername(token);

        UserDTO user = userService.findUserByEmail(username);

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("valid", true);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtTokenUtil.extractUsername(token);
        return ResponseEntity.ok(userService.findUserByEmail(email));
    }
}