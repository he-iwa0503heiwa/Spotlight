package com.eventshare.app.dto.response;

import com.eventshare.app.entity.EventParticipation;
import java.time.LocalDateTime;

public class EventParticipationResponse {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long userId;
    private String username;
    private LocalDateTime participatedAt;

    public EventParticipationResponse() {}

    public EventParticipationResponse(Long id, Long eventId, String eventTitle, Long userId, String username, LocalDateTime participatedAt) {
        this.id = id;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.userId = userId;
        this.username = username;
        this.participatedAt = participatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getParticipatedAt() {
        return participatedAt;
    }

    public void setParticipatedAt(LocalDateTime participatedAt) {
        this.participatedAt = participatedAt;
    }
} 