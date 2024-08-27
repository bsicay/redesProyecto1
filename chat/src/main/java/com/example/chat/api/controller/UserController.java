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



    @GetMapping("/discover")
    public ResponseEntity<List<String>> discoverServices(@RequestParam String username) {
        Connection connection = sessionManager.getConnection(username);
        if (connection != null) {
            List<String> services = connection.discoverServices();
            return new ResponseEntity<>(services, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/allMessages")
    public ResponseEntity<List<String>> getAllMessages(@RequestParam String username) {
        Connection connection = sessionManager.getConnection(username);
        if (connection != null) {
            List<String> allMessages = connection.getAllMessages();
            return new ResponseEntity<>(allMessages, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/chatWithUser")
    public ResponseEntity<List<String>> chatWithUser(@RequestParam String username,
                                                     @RequestParam String recipient) {
        Connection connection = sessionManager.getConnection(username);
        if (connection != null) {
            List<String> messageHistory = connection.getMessageHistory(recipient);
            return new ResponseEntity<>(messageHistory, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/sendMessage")
    public ResponseEntity<String> sendMessage(@RequestParam String username,
                                              @RequestParam String recipient,
                                              @RequestParam String message) throws Exception {
        Connection connection = sessionManager.getConnection(username);
        if (connection != null) {
            connection.addUserToChatHistory(recipient);
            connection.sendMessage(recipient, message);
            connection.addMessageToHistory(recipient, "You: " + message);
            return new ResponseEntity<>("Message sent successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to send message", HttpStatus.UNAUTHORIZED);
        }
    }


    @GetMapping("/searchAndAddContact")
    public ResponseEntity<List<User>> searchAndAddContact(@RequestParam String username, @RequestParam String searchTerm) {
        Connection connection = sessionManager.getConnection(username);
        if (connection == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            List<User> foundUsers = connection.searchUsers(searchTerm);

            return new ResponseEntity<>(foundUsers, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/addContact")
    public ResponseEntity<String> addContact(@RequestParam String username, @RequestParam String contactJid) {
        Connection connection = sessionManager.getConnection(username);
        if (connection != null) {
            connection.sendSubscription(contactJid);
            return new ResponseEntity<>("Subscription request sent successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to send subscription request", HttpStatus.UNAUTHORIZED);
        }
    }




}  