package com.example.chat.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SessionManager {

    private final Map<String, Connection> userConnections = new HashMap<>();

    public Connection getConnection(String username) {
        return userConnections.get(username);
    }

    public Connection createConnection(String username, String password) {
        Connection connection = new Connection();
        connection.connect("alumchat.lol");
        int loginResult = connection.login(username, password);

        if (loginResult == 0) {
            userConnections.put(username, connection);
            return connection;
        } else {
            return null;  // Indica un fallo de autenticación
        }
    }

    public void removeConnection(String username) {
        userConnections.remove(username);
    }

}
