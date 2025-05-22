package com.example.tfg.Domain; // o com.example.tfg.Models

import java.util.Date;

public class Message {
    private String senderId;
    private String text;
    private Date timestamp;

    public Message() {}  // Constructor vacío requerido por Firestore

    public Message(String senderId, String text, Date timestamp) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters y setters

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
