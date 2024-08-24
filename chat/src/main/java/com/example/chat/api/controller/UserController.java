package com.example.chat.api.controller;

import com.example.chat.service.Connection;
//import com.example.chat.service.ClientManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class UserController {

    private final Connection connection;

    public UserController() {
        this.connection = new Connection();
        connection.connect("alumchat.lol");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String username, @RequestParam String password) {
        int result = connection.login(username, password);

        Map<String, String> response = new HashMap<>();
        if (result == 0) {
            response.put("message", "Inicio de sesión exitoso.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("message", "Credenciales inválidas. No puedes iniciar sesión.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // Otros endpoints para registro, acciones después de autorización, etc.
}