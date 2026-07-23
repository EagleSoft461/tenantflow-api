package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED) // Testin beklediği 201 statüsünü döner
    public User registerUser(@RequestBody User user) {
        // Gelen kullanıcıyı veritabanına kaydetmesi için servise gönderiyoruz
        return userService.saveUser(user);
    }
}