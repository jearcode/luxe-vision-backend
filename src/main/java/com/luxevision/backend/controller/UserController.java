package com.luxevision.backend.controller;

import com.luxevision.backend.configuration.JwtUtil;
import com.luxevision.backend.dto.LoginRequest;
import com.luxevision.backend.entity.User;
import com.luxevision.backend.entity.UserRole;
import com.luxevision.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userService.isEmailTaken(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email ya registrado.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUserRole(UserRole.ROLE_USER); // Rol de usuario por defecto
        userService.saveUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado con exito.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        System.out.println("Intentando iniciar sesión con el email: " + loginRequest.getEmail());
        User user = userService.findByEmail(loginRequest.getEmail());
        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user);
            System.out.println("Token generado: " + token);
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        }
        System.out.println("Credenciales no válidas.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales Invalidas.");
    }


}
