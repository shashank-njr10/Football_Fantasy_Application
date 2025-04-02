package com.example.fantasy_football_backend.services;

import com.example.fantasy_football_backend.dto.UserDTO;
import com.example.fantasy_football_backend.exceptions.ResourceNotFoundException;
import com.example.fantasy_football_backend.models.User;
import com.example.fantasy_football_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDTO(user);
    }

    public UserDTO findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToDTO(user);
    }

    public UserDTO createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            // Handle existing user login instead of creation
            return mapToDTO(userRepository.findByEmail(user.getEmail()).get());
        }

        return mapToDTO(userRepository.save(user));
    }

    public UserDTO updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setUsername(userDetails.getUsername());
        user.setFullName(userDetails.getFullName());
        user.setProfilePicture(userDetails.getProfilePicture());

        return mapToDTO(userRepository.save(user));
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public boolean makeAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setAdmin(true);
        userRepository.save(user);
        return true;
    }

    public boolean removeAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setAdmin(false);
        userRepository.save(user);
        return true;
    }

    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setAdmin(user.isAdmin());
        return dto;
    }
}