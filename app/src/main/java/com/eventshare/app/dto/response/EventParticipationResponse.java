package com.eventshare.app.dto.response;

import com.eventshare.app.entity.EventParticipation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventParticipationResponse {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long userId;
    private String username;
    private String status;
    private LocalDateTime participatedAt;
    //イベント情報
    private EventInfo event;
    //ユーザー情報
    private UserInfo user;

    /*
    イベント情報の内部クラス
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventInfo{
        private Long id;
        private String title;
        private String description;
        private LocalDateTime eventDate;
        private String location;
        private Integer capacity;
        private Integer participantCount;
    }

    /*
    ユーザー情報の内部クラス
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo{
        private Long id;
        private String username;
    }
}