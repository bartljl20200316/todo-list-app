package com.sf.todo.controller;

import com.sf.todo.dto.JwtAuthResponse;
import com.sf.todo.dto.LoginDto;
import com.sf.todo.dto.SignUpDto;
import com.sf.todo.dto.SignUpUserDto;
import com.sf.todo.exception.ValidationException;
import com.sf.todo.model.Role;
import com.sf.todo.model.User;
import com.sf.todo.repository.UserRepository;
import com.sf.todo.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.generateToken(authentication);
            return ResponseEntity.ok(new JwtAuthResponse(token));
        }catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpDto signUpDto) {
        if (userRepository.findByUsername(signUpDto.getUsername()).isPresent()) {
            throw new ValidationException("Username is already taken", List.of(signUpDto.getUsername() + " is already taken"));
        }
        if (userRepository.findByEmail(signUpDto.getEmail()).isPresent()) {
            throw new ValidationException("Email is already taken", List.of(signUpDto.getEmail() + " is already taken"));
        }

        User user = new User();
        user.setUsername(signUpDto.getUsername());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        user.setRoles(Collections.singleton(Role.ROLE_USER));

        user = userRepository.save(user);
        SignUpUserDto userDto = new SignUpUserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRoles());

        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }
}
