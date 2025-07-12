package com.eventshare.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
Photoエンティティ
イベントに投稿された写真情報を管理
 */
@Entity
@Table(name = "photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {

    //主キーを定義
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//IDは自動採番（AUTO_INCREMENT）で生成
    private Long id;

    //サーバーに保存される際の実際のファイル名フィールド
    @NotBlank(message = "ファイル名は必須です")
    @Size(max = 255, message = "ファイル名は255字以内で入力してください")
    @Column(name = "filename", nullable = false)//データベースのfilenameカラムにマッピング
    private String filename;

    //ユーザーが登録するファイル名
    @NotBlank(message = "挿入するファイル名は必須です")
    @Size(max = 255, message = "ファイル名は255字以内で入力してください")
    @Column(name = "uploadFilename")
    private String uploadFilename;

    //説明文フィールド
    @Size(max = 500, message = "キャプションは500字以内にしてください")
    @Column(name = "caption")
    private String caption;

    //ファイル情報フィールド
    @Column(name = "fileSize")
    private Long fileSize;

    //MINEタイプ：ファイル形式を示す情報（ブラウザがどう表示するかの判断に使用）
    @Size(max = 50, message = "MINEタイプは50字以内で入力してください")
    @Column(name = "mineType")
    private String mineType;

    //日時フィールド
    @Column(name = "uploadedAt")
    private LocalDateTime uploadedAt;

    /*
    外部キー
    リレーションシップの定義
     */
    //この写真が投稿されたイベント 写真（多）：イベント（１）
    @ManyToOne(fetch = FetchType.LAZY)//必要な時だけデータを取得（遅延読み込み）
    @JoinColumn(name = "event_id", nullable = false)//event_idカラムで結合
    private Event event;

    //この写真をアップロードしたユーザー 写真（多）：イベント（１）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)//uploaded_byカラムで結合
    private User uploadedBy;


    //初回保存する前に実行するライフサイクルメソッド
    @PrePersist  //データベースに初回保存する直前に実行
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();  //現在日時を自動設定
        }
    }
}
