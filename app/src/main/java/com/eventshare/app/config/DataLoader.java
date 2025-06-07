package com.eventshare.app.config;

import com.eventshare.app.entity.EventCategory;
import com.eventshare.app.service.EventCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/*
 アプリケーション起動時に初期データを投入するクラス
 開発・テスト環境でのデータ準備に使用
 */
@Component
public class DataLoader implements CommandLineRunner {
    private final EventCategoryService eventCategoryService;

    @Autowired
    public DataLoader(EventCategoryService eventCategoryService) {
        this.eventCategoryService = eventCategoryService;
    }

    //Spring Boot起動に自動実行するメソッド
    @Override
    public void run(String... args) throws Exception {
        loadInitialCategories();
    }

    /*
    初期のイベントカテゴリデータを投入するメソッド
     */
    private void loadInitialCategories() {
        if (eventCategoryService.getAllCategories().isEmpty()) {
            // 野球カテゴリ
            if (!eventCategoryService.existsByName("野球")) {
                EventCategory baseball = new EventCategory();
                baseball.setName("野球");
                baseball.setDescription("プロ野球観戦　阪神対ヤクルト　神宮球場");
                eventCategoryService.createCategory(baseball);
                System.out.println("初期カテゴリを作成しました: 野球");
            }
            if (!eventCategoryService.existsByName("写真・カメラ")) {
                EventCategory camera = new EventCategory();
                camera.setName("写真・カメラ");
                camera.setDescription("FUJIFilmのミラーレスカメラで写真を撮る");
                eventCategoryService.createCategory(camera);
                System.out.println("初期カテゴリを作成しました：　写真・カメラ");
            }
            if (!eventCategoryService.existsByName("お笑い")) {
                EventCategory comedy = new EventCategory();
                comedy.setName("お笑い");
                comedy.setDescription("お笑いライブに足を運ぶ会");
                eventCategoryService.createCategory(comedy);
                System.out.println("初期カテゴリを作成しました：　お笑い");
            }
            if (!eventCategoryService.existsByName("その他")) {
                EventCategory others = new EventCategory();
                others.setName("その他");
                others.setDescription("上記カテゴリに該当しないその他のイベント");
                eventCategoryService.createCategory(others);
                System.out.println("初期カテゴリを作成しました: その他");
            }
            System.out.println("初期カテゴリの作成が完了しました");
        } else {
            System.out.println("初期カテゴリは既に存在します。初期データ投入をスキップします");
        }
    }
}

