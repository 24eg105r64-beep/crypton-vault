package com.crypton.controller;

import com.crypton.dto.RegisterDto;
import com.crypton.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "registered", required = false) String registered,
                            Model model) {
        if (error != null) {
            if ("locked".equalsIgnoreCase(error)) {
                model.addAttribute("errorMessage",
                        "Your account has been locked for 15 minutes due to 5 consecutive failed login attempts. Please try again later.");
            } else {
                model.addAttribute("errorMessage", "Invalid email or password.");
            }
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "You've been signed out.");
        }

        if (registered != null) {
            model.addAttribute("successMessage", "Registration successful! Please log in.");
        }

        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerDto")) {
            model.addAttribute("registerDto", new RegisterDto());
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("registerDto") RegisterDto registerDto,
                                      BindingResult bindingResult,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {
        // Password confirmation check
        if (registerDto.getPassword() != null &&
                !registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }

        // Email uniqueness check
        if (registerDto.getEmail() != null &&
                userService.existsByEmail(registerDto.getEmail())) {
            bindingResult.rejectValue("email", "email.duplicate", "An account with this email already exists. Please log in instead.");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        String deviceInfo = request.getHeader("User-Agent");
        userService.registerUser(registerDto, deviceInfo);

        redirectAttributes.addAttribute("registered", "true");
        return "redirect:/login";
    }
}
