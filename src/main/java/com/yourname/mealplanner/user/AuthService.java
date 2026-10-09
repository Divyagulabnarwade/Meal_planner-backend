package com.yourname.mealplanner.user;

import com.yourname.mealplanner.common.BadRequestException;
import com.yourname.mealplanner.config.JwtTokenProvider;
import com.yourname.mealplanner.user.dto.AuthResponse;
import com.yourname.mealplanner.user.dto.LoginRequest;
import com.yourname.mealplanner.user.dto.RegisterRequest;
import com.yourname.mealplanner.user.dto.UserSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .build();

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateTokenFromUser(
                savedUser.getId(), savedUser.getEmail(), savedUser.getFullName());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(UserSummaryDto.builder()
                        .id(savedUser.getId())
                        .email(savedUser.getEmail())
                        .fullName(savedUser.getFullName())
                        .build())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String token = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(UserSummaryDto.builder()
                        .id(userPrincipal.getId())
                        .email(userPrincipal.getEmail())
                        .fullName(userPrincipal.getFullName())
                        .build())
                .build();
    }

    public UserSummaryDto getCurrentUser(UserPrincipal principal) {
        if (principal == null) {
            throw new BadRequestException("User is not authenticated");
        }
        return UserSummaryDto.builder()
                .id(principal.getId())
                .email(principal.getEmail())
                .fullName(principal.getFullName())
                .build();
    }
}
