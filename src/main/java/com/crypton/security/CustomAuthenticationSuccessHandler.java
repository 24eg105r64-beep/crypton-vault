package com.crypton.security;

import com.crypton.model.User;
import com.crypton.repository.UserRepository;
import com.crypton.service.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName();
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getFailedAttempts() > 0 || user.getLockedUntil() != null) {
                user.setFailedAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            }

            // Record LOGIN audit log
            String deviceInfo = request.getHeader("User-Agent");
            auditService.logAction(user, "LOGIN", deviceInfo);
        }

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
