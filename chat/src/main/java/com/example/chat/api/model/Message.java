package com.example.chat.api.model;

public class Message {
    private User sender;
    private String time;
    private String text;
    private boolean unread;

    public Message(User sender, String time, String text, boolean unread) {
        this.sender = sender;
        this.time = time;
        this.text = text;
        this.unread = unread;
    }

    // Getters y setters

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }
}