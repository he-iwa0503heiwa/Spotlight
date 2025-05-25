package com.eventshare.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 イベント情報レスポンスのためのDTOクラス
 フロントエンドにイベント情報を返すためのクラス
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private String location;
    private Integer capacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // カテゴリ情報
    private CategoryInfo category;

    // 作成者情報
    private CreatorInfo creator;

    // 参加者数
    private Integer participantCount;

    /*
     カテゴリ情報の内部クラス
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String description;
    }

    /*
     作成者情報の内部クラス
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatorInfo {
        private Long id;
        private String username;
        private String profilePicture;
    }
}