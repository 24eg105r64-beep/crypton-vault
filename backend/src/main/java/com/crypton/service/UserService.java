package com.crypton.service;

import com.crypton.dto.RegisterDto;
import com.crypton.model.User;

import java.util.Optional;

public interface UserService {

    User registerUser(RegisterDto registerDto, String deviceInfo);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
