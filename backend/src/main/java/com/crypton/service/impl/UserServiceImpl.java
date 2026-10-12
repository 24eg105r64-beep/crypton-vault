package com.crypton.service.impl;

import com.crypton.dto.RegisterDto;
import com.crypton.model.AuditLog;
import com.crypton.model.User;
import com.crypton.repository.AuditLogRepository;
import com.crypton.repository.UserRepository;
import com.crypton.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerUser(RegisterDto registerDto, String deviceInfo) {
        User user = User.builder()
                .name(registerDto.getName().trim())
                .email(registerDto.getEmail().trim().toLowerCase())
                .phone(registerDto.getPhone().trim())
                .passwordHash(passwordEncoder.encode(registerDto.getPassword()))
                .failedAttempts(0)
                .build();

        User savedUser = userRepository.save(user);

        // Record audit log for registration
        AuditLog registrationLog = AuditLog.builder()
                .user(savedUser)
                .action("USER_REGISTERED")
                .deviceInfo(deviceInfo != null ? deviceInfo : "Web Browser")
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(registrationLog);

        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }
}
