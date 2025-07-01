package com.eventshare.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    //ファイル情報フィールド

    //ファイル形式を示す情報（ブラウザがどう表示するかの判断に使用）

    //日時フィールド

}
