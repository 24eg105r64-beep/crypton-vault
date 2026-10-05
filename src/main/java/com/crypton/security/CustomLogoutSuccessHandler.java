package com.crypton.security;

import com.crypton.model.User;
import com.crypton.repository.UserRepository;
import com.crypton.service.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        if (authentication != null && authentication.getName() != null) {
            Optional<User> userOptional = userRepository.findByEmail(authentication.getName());
            userOptional.ifPresent(user -> {
                String deviceInfo = request.getHeader("User-Agent");
                auditService.logAction(user, "LOGOUT", deviceInfo);
            });
        }

        response.sendRedirect(request.getContextPath() + "/login?logout");
    }
}
