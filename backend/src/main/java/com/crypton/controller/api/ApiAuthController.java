package com.crypton.controller.api;

import com.crypton.dto.RegisterDto;
import com.crypton.dto.api.ApiDtos.LoginRequest;
import com.crypton.dto.api.ApiDtos.LoginResponse;
import com.crypton.dto.api.ApiDtos.MessageResponse;
import com.crypton.dto.api.ApiDtos.UserView;
import com.crypton.model.User;
import com.crypton.repository.UserRepository;
import com.crypton.security.ApiTokenService;
import com.crypton.security.CustomAuthenticationFailureHandler;
import com.crypton.security.CustomUserDetails;
import com.crypton.service.AuditService;
import com.crypton.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Sign-in, registration and "who am I" for the React frontend.
 * Reuses the same UserService, password check and lockout rules as the
 * original login page (5 failed attempts = 15 minute lock).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private static final String LOCKED_MESSAGE =
            "For your safety, this account is paused for 15 minutes after several attempts.";
    private static final String BAD_CREDENTIALS_MESSAGE =
            "That email and password don't match. Please try again.";

    private final AuthenticationManager authenticationManager;
    private final ApiTokenService tokenService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body, HttpServletRequest request) {
        if (body == null || body.email() == null || body.email().isBlank()
                || body.password() == null || body.password().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Please enter your email and password."));
        }

        String email = body.email().trim().toLowerCase();
        String deviceInfo = request.getHeader("User-Agent");

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, body.password()));
            User user = ((CustomUserDetails) auth.getPrincipal()).getUser();

            if (user.getFailedAttempts() > 0 || user.getLockedUntil() != null) {
                user.setFailedAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            }
            auditService.logAction(user, "LOGIN", deviceInfo);

            return ResponseEntity.ok(new LoginResponse(tokenService.issueToken(user.getUserId()), UserView.of(user)));

        } catch (LockedException e) {
            userRepository.findByEmail(email)
                    .ifPresent(user -> auditService.logAction(user, "FAILED_LOGIN", deviceInfo));
            return ResponseEntity.status(HttpStatus.LOCKED).body(new MessageResponse(LOCKED_MESSAGE));

        } catch (AuthenticationException e) {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                auditService.logAction(user, "FAILED_LOGIN", deviceInfo);

                int attempts = user.getFailedAttempts() + 1;
                user.setFailedAttempts(attempts);
                if (attempts >= CustomAuthenticationFailureHandler.MAX_FAILED_ATTEMPTS) {
                    user.setLockedUntil(LocalDateTime.now()
                            .plusMinutes(CustomAuthenticationFailureHandler.LOCK_TIME_DURATION_MINUTES));
                    userRepository.save(user);
                    return ResponseEntity.status(HttpStatus.LOCKED).body(new MessageResponse(LOCKED_MESSAGE));
                }
                userRepository.save(user);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(BAD_CREDENTIALS_MESSAGE));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto registerDto, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (registerDto.getPassword() != null
                && !registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            errors.put("confirmPassword", "Passwords do not match");
        }
        if (userService.existsByEmail(registerDto.getEmail())) {
            errors.put("email", "An account with this email already exists. Please log in instead.");
        }
        if (!errors.isEmpty()) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", 400);
            response.put("message", "Please check the highlighted fields.");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }

        userService.registerUser(registerDto, request.getHeader("User-Agent"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Your vault is ready. Sign in to open it."));
    }

    @GetMapping("/me")
    public UserView me(@AuthenticationPrincipal CustomUserDetails principal) {
        return UserView.of(principal.getUser());
    }

    /** Records the sign-out. The frontend discards its token. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserDetails principal,
                                       HttpServletRequest request) {
        auditService.logAction(principal.getUser(), "LOGOUT", request.getHeader("User-Agent"));
        return ResponseEntity.noContent().build();
    }
}
