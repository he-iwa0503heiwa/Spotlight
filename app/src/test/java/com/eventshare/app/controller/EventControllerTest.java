package com.eventshare.app.controller;

import com.eventshare.app.config.TestSecurityConfig;
import com.eventshare.app.dto.request.EventRequest;
import com.eventshare.app.dto.response.EventResponse;
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
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
@ActiveProfiles("test") //test用のapplocation.propertiesの設定ファイルを読み込む
@Import(TestSecurityConfig.class)
public class EventControllerTest {
    /*
    テストデータの準備
     */
    private ObjectMapper objectMapper;  //JSON変換用
    //モックツール
    private MockMvc mockMvc;
    //依存するサービスをモックか
    @Mock
    private EventService eventService;
    @Mock
    private EventCategoryService eventCategoryService;
    @Mock
    private UserService userService;
    @Mock
    private EventParticipationService eventParticipationService;
    private User testUser;              //テスト用ユーザー
    private EventCategory testCategory; //テスト用カテゴリ
    private Event testEvent;           //テスト用イベント

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
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        //カテゴリデータ
        testCategory = new EventCategory();
        testCategory.setId(1L);
        testCategory.setName("野球");
        testCategory.setDescription("野球イベント");

        //イベントデータ
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("阪神vs巨人");
        testEvent.setDescription("プロ野球観戦");
        testEvent.setEventDate(LocalDateTime.now().plusDays(7));
        testEvent.setLocation("甲子園球場");
        testEvent.setCapacity(50);
        testEvent.setCategory(testCategory);
        testEvent.setCreatedAt(LocalDateTime.now());
        testEvent.setUpdatedAt(LocalDateTime.now());
    }

    /*
    イベント一覧取得のテスト
    1. GET /api/events にアクセスできるか
    2. 正常なレスポンス（200 OK）が返るか
    3. JSON形式で配列が返るか
    4. イベントデータが正しく含まれているか
    5. 参加者数が正しく計算されているか
     */
    @Test
    @WithMockUser(username = "testuser")
    void testGetAllEvents() throws Exception {
        //モックの動作設定
        List<Event> events = Arrays.asList(testEvent); //配列をリストに
        when(eventService.getAllEvents()).thenReturn(events);//getAllEvents()でeventsを返す
        when(eventParticipationService.getParticipantCountForEvent(any(Event.class))).thenReturn(0);//getParticipantCountForEventが呼ばれたら0を返す

        //仮のwebサーバー作ってapiを呼び出し、レスポンスをチェック
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("阪神vs巨人"))
                .andExpect(jsonPath("$[0].participantCount").value(0));
    }

    /*
    イベント作成のテスト
    1. 認証済みユーザーがイベントを作成できるか
    2. JSON形式のリクエストボディを正しく処理できるか
    3. バリデーションが正しく動作するか
    4. 作成後のレスポンスが正しいか
    5. HTTP 201 Created が返るか
     */
    @Test
    @WithMockUser(username = "testuser")
    void testCreateEvent() throws Exception {
        //念のためIDを明示的にセット
        testUser.setId(1L);
        testEvent.setId(1L);
        testEvent.setCreator(testUser);
        testEvent.setCategory(testCategory);
        testEvent.setTitle("新しいイベント");
        testEvent.setDescription("テストイベント");
        testEvent.setEventDate(LocalDateTime.now().plusDays(10));
        testEvent.setLocation("テスト会場");
        testEvent.setCapacity(30);

        //リクエストデータの作成
        EventRequest eventRequest = new EventRequest();
        eventRequest.setTitle("新しいイベント");
        eventRequest.setDescription("テストイベント");
        eventRequest.setEventDate(LocalDateTime.now().plusDays(10));
        eventRequest.setLocation("テスト会場");
        eventRequest.setCategoryId(1L);
        eventRequest.setCapacity(30);

        //モックの動作設定
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(eventCategoryService.getCategoryById(1L)).thenReturn(testCategory);
        when(eventService.createEvent(any(Event.class))).thenReturn(testEvent);

        //POST apiを呼び出してレスポンスをチェック
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))  //JSON変換
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("新しいイベント"))
                .andExpect(jsonPath("$.category.name").value("野球"));
    }
}