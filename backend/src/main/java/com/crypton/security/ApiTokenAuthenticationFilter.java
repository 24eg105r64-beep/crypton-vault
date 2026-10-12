package com.crypton.security;

import com.crypton.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads "Authorization: Bearer <token>" on /api/** requests and, if the token
 * is valid, signs the request in as that user.
 *
 * Deliberately NOT a @Component, so it only runs inside the API security chain
 * and never on the Thymeleaf pages.
 */
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

    private final ApiTokenService tokenService;
    private final UserRepository userRepository;

    public ApiTokenAuthenticationFilter(ApiTokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            tokenService.verifyToken(token)
                    .flatMap(userRepository::findById)
                    .filter(user -> user.isAccountNonLocked())
                    .ifPresent(user -> {
                        CustomUserDetails details = new CustomUserDetails(user);
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                details, null, details.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }
        filterChain.doFilter(request, response);
    }
}
