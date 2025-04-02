package com.example.fantasy_football_backend.controllers;

import com.example.fantasy_football_backend.dto.UserDTO;
import com.example.fantasy_football_backend.models.User;
import com.example.fantasy_football_backend.services.AnalyticsService;
import com.example.fantasy_football_backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.findUserByEmail(email));
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody User user) {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @PostMapping("/{id}/make-admin")
    public ResponseEntity<Boolean> makeAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(userService.makeAdmin(id));
    }

    @PostMapping("/{id}/remove-admin")
    public ResponseEntity<Boolean> removeAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(userService.removeAdmin(id));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<AnalyticsService.UserPerformanceStats> getUserStats(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getUserPerformanceStats(id));
    }
}