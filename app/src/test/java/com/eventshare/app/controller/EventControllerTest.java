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

    /*
    テストデータの準備
     */
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
        testUser.setName("testuser");

        //カテゴリデータ
        testCategory = new EventCategory;
        testCategory.setId(1L);
        testCategory.setName("野球");
        testCategory.setDescription("野球イベント");

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
    void testGetAllEvents() throws Excetion {
        //モックの動作設定
        List<Event> events = Arrays.asList(testEvent);　//配列をリストに
        when(eventService.getAllEvents()).thenReturn(events);//getAllEvents()でeventsを返す
        when(eventParticipationService.getParticipantCountForEvent(any(Event.class))).thenReturn(5);//getParticipantCountForEventが呼ばれたら5を返す

        //仮のwebサーバー作ってapiを呼び出し、レスポンスをチェック
        mockMvc.perform(get("/api/Events"))
                .andExcept(status().isOk())
                .andExcept(jsonpath("$").isArray())
                .andExcept(jsonpath("$[0].id").value(1))
                .andExcept(jsonpath("$[0].title").value("阪神vs巨人"))
                .andExcept(jsonpath("$[0].participantCount").value(5);
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
    @WithMockUser(username = "testuser")//認証済みユーザーとしてテスト
    void testCreateEvent() throws Exception {
        //リクエストデータの作成
        EventRequest eventRequest = new EventRequest();
        eventRequest.setTitle("新しいイベント");
        eventRequest.setDescription("テストイベント");
        eventRequest.setEventDate(LocalDateTime.now().plusDays(10));
        eventRequest.setLocation("テスト会場");
        eventRequest.setCategoryId(1L);
        eventRequest.setCapacity(30);

        //モックの動作を設定
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(eventCategoryService.getCategoryById(1L)).thenReturn(testCategory);
        when(eventService.createEvent(any(Event.class))).thenReturn(testEvent);
        when(eventParticipationService.getParticipantCountForEvent(any(Event.class))).thenReturn(0);

        //POST apiを呼び出してレスポンスをチェック
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))  //JSON変換
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("阪神vs巨人"))
                .andExpect(jsonPath("$.category.name").value("野球"));
    }
}