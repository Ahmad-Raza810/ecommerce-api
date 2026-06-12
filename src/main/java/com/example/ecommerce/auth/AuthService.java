package com.example.ecommerce.auth;

import com.example.ecommerce.auth.dto.AuthResponse;
import com.example.ecommerce.auth.dto.LoginRequest;
import com.example.ecommerce.auth.dto.RefreshTokenRequest;
import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.security.JwtService;
import com.example.ecommerce.security.RefreshToken;
import com.example.ecommerce.security.RefreshTokenRepository;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserMapper;
import com.example.ecommerce.user.UserRepository;
import com.example.ecommerce.user.UserService;
import com.example.ecommerce.user.dto.UserRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final long refreshTokenDays;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       RefreshTokenRepository refreshTokenRepository,
                       UserRepository userRepository,
                       UserService userService,
                       UserMapper userMapper,
                       @Value("${app.jwt.refresh-token-days}") long refreshTokenDays) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.userMapper = userMapper;
        this.refreshTokenDays = refreshTokenDays;
    }

    @Transactional
    public AuthResponse register(UserRequestDto request) {
        userService.register(request);
        User user = findByEmail(request.email());
        return tokensFor(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(Locale.ROOT), request.password()));
        User user = findByEmail(request.email());
        return tokensFor(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED));
        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Refresh token expired or revoked", HttpStatus.UNAUTHORIZED);
        }
        refreshToken.setRevoked(true);
        return tokensFor(refreshToken.getUser());
    }

    private AuthResponse tokensFor(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plusSeconds(refreshTokenDays * 24 * 60 * 60));
        refreshTokenRepository.save(refreshToken);
        return new AuthResponse(jwtService.generateAccessToken(user), refreshToken.getToken(), "Bearer",
                userMapper.toResponse(user));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED));
    }
}
