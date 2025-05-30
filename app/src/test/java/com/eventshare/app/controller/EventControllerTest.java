package com.eventshare.app.controller;

import com.eventshare.app.config.TestSecurityConfig;
import com.eventshare.app.dto.request.EventRequest;
import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.EventCategory;
import com.eventshare.app.entity.User;
import com.eventshare.app.service.EventService;
import com.eventshare.app.service.EventCategoryService;
import com.eventshare.app.service.UserService;
import com.eventshare.app.service.EventParticipationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 EventControllerのテストクラス
 */
@WebMvcTest(EventController.class) //コントローラー専用テストのアノテーション
@Import(TestSecurityConfig.class) //必要な設定クラスを手動で読み込む
@ActiveProfiles("test") //test用のapplocation.propertiesの設定ファイルを読み込む
public class EventControllerTest {
    //モックツール
    private MockMvc mockMvc;
    //依存するサービスをモックか
    @MockBean
    private EventService eventService;
    @MockBean
    private EventCategoryService eventCategoryService;
    @MockBean
    private UserService userService;
    @MockBean
    private EventParticipationService eventParticipationService;
    //テストデータの準備
    private ObjectMapper objectMapper;  //JSON変換用
    private User testUser;              //テスト用ユーザー
    private EventCategory testCategory; //テスト用カテゴリ
    private Event testEvent;           //テスト用イベント

    //コンストラクタインジェクション
    @Autowired
    public EventControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    //テストメソッド実行前のセットアップ
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();//javaオブジェクトとjsonを変換
        objectMapper.registerModule(new JavaTimeModule());

        //ユーザーデータ
        testUser = new User;
        testUser.setId(1L);
        testUser.setName("testUser");

        //イベントデータ
        testEvent = new Event;
        testEvent.setId(1L);
        testEvent.setTitle("阪神vs巨人");
        testEvent.setDescription("プロ野球観戦");
        testEvent.setEventDate(LocalDateTime.now().plusDays(7));
        testEvent.setLocation("甲子園球場");
        testEvent.setCapacity(50);
        testEvent.setCategory(testCategory);
        testEvent.setCreator(testUser);
        testEvent.setCreatedAt(LocalDateTime.now());
        testEvent.setUpdatedAt(LocalDateTime.now());

        //カテゴリデータ
        testCategory = new EventCategory;
        testCategory.setId(1L);
        testCategory.setName("野球");
        testCategory.setDescription("野球イベント");
    }
}