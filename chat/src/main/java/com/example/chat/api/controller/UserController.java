package com.example.chat.api.controller;
import com.example.chat.service.SessionManager;
import com.example.chat.service.Connection;
import com.example.chat.api.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


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
        Connection connection = sessionManager.getConnection(username);

        if (connection != null) {
            connection.logout();

        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
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
    public ResponseEntity<String> sendMessage(
            @RequestParam String username,
            @RequestParam String recipient,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String filePath) throws Exception {

        Connection connection = sessionManager.getConnection(username);

        if (connection != null) {
            connection.addUserToChatHistory(recipient);

            if (filePath != null && !filePath.isEmpty()) {
                connection.sendFile(recipient, filePath);
            } else if (message != null && !message.isEmpty()) {
                connection.sendMessage(recipient, message);
                connection.addMessageToHistory(recipient, "You: " + message);
            } else {
                return new ResponseEntity<>("No message or file provided", HttpStatus.BAD_REQUEST);
            }

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


    @GetMapping("/getUserDetail")
    public ResponseEntity<User> getUserDetail(@RequestParam String username, @RequestParam String userJid) {
        Connection connection = sessionManager.getConnection(username);
        if (connection != null) {
            User userDetails = connection.getUserDetail(userJid);
            if (userDetails != null) {
                return new ResponseEntity<>(userDetails, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/createGroupChat")
    public ResponseEntity<String> createGroupChat(@RequestParam String username,
                                                  @RequestParam String chatRoomName,
                                                  @RequestParam String nickname) {
        Connection connection = sessionManager.getConnection(username);
        if (connection != null) {
            connection.createGroupChat(chatRoomName, nickname);
            return new ResponseEntity<>("Group chat created successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to create group chat", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/inviteToGroupChat")
    public ResponseEntity<String> inviteToGroupChat(@RequestParam String username,
                                                    @RequestParam String groupName,
                                                    @RequestParam String user) {
        Connection connection = sessionManager.getConnection(username);
        if (connection != null) {
            connection.inviteToGroupChat(groupName, user);
            return new ResponseEntity<>("User invited to group chat successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to invite user to group chat", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/joinGroupChat")
    public ResponseEntity<String> joinGroupChat(@RequestParam String username,
                                                @RequestParam String chatRoomName,
                                                @RequestParam String nickname) {
        Connection connection = sessionManager.getConnection(username);
        if (connection != null) {
            connection.joinGroupChat(chatRoomName, nickname);
            return new ResponseEntity<>("Joined group chat successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to join group chat", HttpStatus.UNAUTHORIZED);
        }
    }



    @PostMapping("/useGroupChat")
    public ResponseEntity<List<String>> useGroupChat(@RequestParam String username,
                                                     @RequestParam String chatRoomName) {
        Connection connection = sessionManager.getConnection(username);
        if (connection != null) {
            List<String> messageHistory = connection.getGroupChatHistory(chatRoomName);
            return new ResponseEntity<>(messageHistory, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/sendGroupMessage")
    public ResponseEntity<String> sendGroupMessage(@RequestParam String username,
                                                   @RequestParam String chatRoomName,
                                                   @RequestParam String message) {
        Connection connection = sessionManager.getConnection(username);
        if (connection != null) {
            connection.sendGroupMessage(chatRoomName, message);
            return new ResponseEntity<>("Message sent successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to send message", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/presence-options")
    public ResponseEntity<Map<String, List<String>>> getPresenceOptions() {
        Map<String, List<String>> options = new HashMap<>();

        // Opciones para el tipo de presencia
        List<String> presenceTypes = Arrays.asList("Disponible", "No disponible");
        // Opciones para el modo de presencia
        List<String> presenceModes = Arrays.asList("Available", "Away", "Chat", "Extended away", "Do not disturb");

        options.put("types", presenceTypes);
        options.put("modes", presenceModes);

        return new ResponseEntity<>(options, HttpStatus.OK);
    }

    @PostMapping("/set-status-message")
    public ResponseEntity<String> setStatusMessage(@RequestParam String username,
                                                   @RequestParam String message,
                                                   @RequestParam String type,
                                                   @RequestParam String mode) {
        Connection connection = sessionManager.getConnection(username);

        if (connection != null) {
            String[] data = new String[]{message, type, mode};
            connection.setStatusMessage(data);
            return new ResponseEntity<>("Status modified successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to modify status", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<String>> getNotifications(@RequestParam String username) {
        Connection connection = sessionManager.getConnection(username);
        if (connection != null) {
            ConcurrentLinkedQueue<String> notifications = connection.getNotificationQueue();
            List<String> notificationList = new ArrayList<>(notifications);
            notifications.clear(); // Clear notifications after sending them
            return new ResponseEntity<>(notificationList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }



//    @PostMapping("/useGroupChat")
//    public ResponseEntity<String> useGroupChat(@RequestParam String username,
//                                               @RequestParam String chatRoomName) {
//        Connection connection = sessionManager.getConnection(username);
//        if (connection != null) {
//            connection.useGroupChat(chatRoomName);
//            return new ResponseEntity<>("Using group chat", HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Failed to use group chat", HttpStatus.UNAUTHORIZED);
//        }
//    }





}  