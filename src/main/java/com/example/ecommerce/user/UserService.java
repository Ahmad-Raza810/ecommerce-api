package com.example.ecommerce.user;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.common.EntityNotFoundException;
import com.example.ecommerce.email.EmailService;
import com.example.ecommerce.notification.NotificationService;
import com.example.ecommerce.notification.NotificationType;
import com.example.ecommerce.user.dto.UserRequestDto;
import com.example.ecommerce.user.dto.UserResponseDto;
import com.example.ecommerce.user.dto.UserUpdateDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       NotificationService notificationService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @Transactional
    public UserResponseDto register(UserRequestDto request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("Email is already registered", HttpStatus.CONFLICT);
        }

        User user = userMapper.toEntity(request);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        User saved = userRepository.save(user);

        notificationService.create(saved, "User registered successfully", NotificationType.USER_REGISTERED);
        emailService.sendWelcomeEmail(saved);
        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public User getEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUser(Long id) {
        return userMapper.toResponse(getEntity(id));
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto request) {
        User user = getEntity(id);
        if (request.email() != null) {
            String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
            if (!normalizedEmail.equals(user.getEmail()) && userRepository.existsByEmail(normalizedEmail)) {
                throw new BusinessException("Email is already registered", HttpStatus.CONFLICT);
            }
            user.setEmail(normalizedEmail);
        }
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        return userMapper.toResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getEntity(id);
        userRepository.delete(user);
    }
}
