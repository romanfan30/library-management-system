package com.example.library_management.controller;

import com.example.library_management.dto.LoginRequest;
import com.example.library_management.entity.User;
import com.example.library_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
// Аутентификация пользователя
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
// Установка аутентификации в контекст (практика 4: Spring Security создаёт сессию)
            SecurityContextHolder.getContext().setAuthentication(authentication);
// Получение информации о пользователе
            Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());
            if (user.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Успешный вход в систему");
                response.put("userId", user.get().getId());
                response.put("email", user.get().getEmail());
                response.put("role", "USER");

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body("Пользователь не найден");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Неверный email или пароль");
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Успешный выход из системы");
    }
}
