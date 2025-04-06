package com.example.fantasy_football_backend.security;

import com.example.fantasy_football_backend.models.User;
import com.example.fantasy_football_backend.repositories.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Extract user information from OAuth2User
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String pictureUrl = (String) attributes.get("picture");

        // Check if user exists
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            // Update existing user
            User user = userOptional.get();
            user.setFullName(name);
            user.setProfilePicture(pictureUrl);
            userRepository.save(user);
        } else {
            // Create new user
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(email.split("@")[0]); // Default username from email
            newUser.setFullName(name);
            newUser.setProfilePicture(pictureUrl);
            newUser.setAdmin(false); // New users are not admins by default
            userRepository.save(newUser);
        }

        return oAuth2User;
    }
}