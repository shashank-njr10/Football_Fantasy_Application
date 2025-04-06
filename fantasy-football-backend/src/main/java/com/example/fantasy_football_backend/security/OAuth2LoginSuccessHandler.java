package com.example.fantasy_football_backend.security;

import com.example.fantasy_football_backend.models.User;
import com.example.fantasy_football_backend.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    @Value("${app.cors.allowed-origins}")
    private String frontendUrl;

    public OAuth2LoginSuccessHandler(JwtTokenUtil jwtTokenUtil, UserRepository userRepository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        // Generate JWT token
        String token = jwtTokenUtil.generateToken(email, user.isAdmin());

        // Redirect to frontend with token
        String redirectUrl = frontendUrl + "/auth/callback?token=" + token;

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}