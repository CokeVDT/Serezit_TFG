package com.example.tfg.Domain;

import java.util.Date;
import java.util.List;

public class Chat {
    private String id;
    private List<String> participants;
    private String lastMessage;
    private Date lastTimestamp;

    public Chat() {}

    public Chat(String id, List<String> participants, String lastMessage, Date lastTimestamp) {
        this.id = id;
        this.participants = participants;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public List<String> getParticipants() {
        return participants;
    }
    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Date getLastTimestamp() {
        return lastTimestamp;
    }
    public void setLastTimestamp(Date lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    // El método clave que debes tener para obtener el otro usuario:
    public String getOtherUserId(String currentUserUid) {
        if (participants == null) return null;
        for (String uid : participants) {
            if (!uid.equals(currentUserUid)) {
                return uid;
            }
        }
        return null;
    }
}
