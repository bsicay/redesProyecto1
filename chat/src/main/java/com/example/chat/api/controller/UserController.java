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

    @GetMapping("/roster")
    public ResponseEntity<String> getRoster() {
        String roster = connection.getRoster();
        if (!roster.isEmpty()) {
            return new ResponseEntity<>(roster, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No se pudo obtener el roster o el roster está vacío.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}  