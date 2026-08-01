package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.UserRegistrationRequestDTO;
import org.example.dto.UserResponseDTO;
import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED) // Testin beklediği 201 statüsünü döner
    public UserResponseDTO registerUser(@Valid @RequestBody UserRegistrationRequestDTO requestDTO) {
        // Gelen kullanıcıyı veritabanına kaydetmesi için servise gönderiyoruz
        return userService.saveUser(requestDTO);
    }
}