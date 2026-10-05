package com.crypton.security;

import com.crypton.model.User;
import com.crypton.repository.UserRepository;
import com.crypton.service.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int LOCK_TIME_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String email = request.getParameter("username");

        if (email != null && !email.isBlank()) {
            Optional<User> userOptional = userRepository.findByEmail(email.trim());

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Record FAILED_LOGIN audit log
                String deviceInfo = request.getHeader("User-Agent");
                auditService.logAction(user, "FAILED_LOGIN", deviceInfo);

                if (!user.isAccountNonLocked()) {
                    response.sendRedirect(request.getContextPath() + "/login?error=locked");
                    return;
                }

                int attempts = user.getFailedAttempts() + 1;
                user.setFailedAttempts(attempts);

                if (attempts >= MAX_FAILED_ATTEMPTS) {
                    user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_TIME_DURATION_MINUTES));
                    userRepository.save(user);
                    response.sendRedirect(request.getContextPath() + "/login?error=locked");
                    return;
                }

                userRepository.save(user);
            }
        }

        if (exception instanceof LockedException) {
            response.sendRedirect(request.getContextPath() + "/login?error=locked");
        } else {
            response.sendRedirect(request.getContextPath() + "/login?error=bad_credentials");
        }
    }
}
