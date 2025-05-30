package com.eventshare.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/*
 イベント作成・更新リクエストのためのDTOクラス
 フロントエンドから送信されるイベントデータを受け取るためのクラス
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {
    @NotBlank(message = "イベントタイトルは必須です")
    @Size(max = 50, message = "タイトルは50文字以内で入力してください")
    private String title;

    @Size(max = 1000, message = "説明は1000文字以内で入力してください")
    private String description;

    @NotNull(message = "イベント開催日時は必須です")
    @Future(message = "イベント開催日時は未来の日時を指定してください")
    private LocalDateTime eventDate;

    @Size(max = 100, message = "開催場所は100文字以内で入力してください")
    private String location;

    @NotNull(message = "カテゴリIDは必須です")
    private Long categoryId;

    private Integer capacity; // 定員（null許容）
}