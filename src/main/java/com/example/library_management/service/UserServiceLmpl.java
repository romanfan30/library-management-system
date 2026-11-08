package com.example.library_management.service;

import com.example.library_management.entity.User;
import com.example.library_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceLmpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Пароль сохраняем только в виде bcrypt-хеша (практика 4: аутентификация
     * сравнивает пароль из запроса с хешем через BCryptPasswordEncoder).
     * Если в поле уже лежит bcrypt-хеш (например, при обновлении существующего
     * пользователя), повторно не кодируем.
     */
    @Override
    public User save(User user) {
        String password = user.getPassword();
        if (password != null && !isBcrypt(password)) {
            user.setPassword(passwordEncoder.encode(password));
        }
        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private boolean isBcrypt(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
