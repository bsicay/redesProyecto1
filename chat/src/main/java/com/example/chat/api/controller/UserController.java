package com.example.chat.api.controller;
import com.example.chat.service.SessionManager;
import com.example.chat.service.Connection;
import com.example.chat.api.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class UserController {

    private  SessionManager sessionManager = new SessionManager();

    public UserController() {
        this.sessionManager = sessionManager;

    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String username, @RequestParam String password) {
        Connection connection = sessionManager.createConnection(username, password);

        Map<String, Object> response = new HashMap<>();
        if (connection != null) {
            response.put("success", true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("success", false);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestParam String username) {
        sessionManager.removeConnection(username);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/roster")
    public ResponseEntity<List<User>> getRoster(@RequestParam String username) {
        Connection connection = sessionManager.getConnection(username);
        if (connection != null) {
            List<User> roster = connection.getRoster();
            return new ResponseEntity<>(roster, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

}  