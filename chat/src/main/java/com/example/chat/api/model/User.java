package com.example.chat.api.model;

public class User {
    private String name;
    private String userJid;
    private String status;
    private String mode;
    private String statusMessage;

    public User(String name, String userJid, String status, String mode, String statusMessage) {
        this.name = name;
        this.userJid = userJid;
        this.status = status;
        this.mode = mode;
        this.statusMessage = statusMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserJid() {
        return userJid;
    }

    public void setUserJid(String userJid) {
        this.userJid = userJid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public String toString() {
        return name + " (" + userJid + ")\n   * Status: " + status + "\n   * Mode: " + mode + "\n   * Status Message: " + statusMessage;
    }
}
