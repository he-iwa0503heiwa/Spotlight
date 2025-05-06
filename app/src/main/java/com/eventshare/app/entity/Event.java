package com.eventshare.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="events")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    private String location;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private EventCategory category;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private User creator;

    private Integer capacity;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "update_at")
    private LocalDateTime updatedAt;

    // リレーションシップの定義
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<EventParticipation> participations = new ArrayList<>();

//    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
//    private List<Photo> photos = new ArrayList<>();
//
//    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
//    private List<Comment> comments = new ArrayList<>();
//
//    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
//    private List<Favorite> favorites = new ArrayList<>();
}