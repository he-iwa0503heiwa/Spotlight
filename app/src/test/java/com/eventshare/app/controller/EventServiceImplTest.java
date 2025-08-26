package com.eventshare.app.controller;

import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.EventCategory;
import com.eventshare.app.entity.User;
import com.eventshare.app.repository.EventRepository;
import com.eventshare.app.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/*
EventServiceImplのテストクラス
ビジネスロジックの単体テスト
 */
@ExtendWith(MockitoExtension.class)//Mockitoでのテスト（Spring Bootなし）
public class EventServiceImplTest {
    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event testEvent;
    private User testUser;
    private EventCategory testCategory;

    //初期セット
    @BeforeEach
    void setUp() {
        //テスト用ユーザー
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        //テスト用カテゴリ
        testCategory = new EventCategory();
        testCategory.setId(1L);
        testCategory.setName("testcategory");

        //テスト用イベント
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("testtitle");
        testEvent.setDescription("testdescription");
        testEvent.setEventDate(LocalDateTime.now().plusDays(7));
        testEvent.setCapacity(50);
        testEvent.setCategory(testCategory);
        testEvent.setCreator(testUser);
    }

    //イベント情報取得テスト
    @Test
    void testGetAllEvents() {
        //モックの設定
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findAll()).thenReturn(events);

        //テスト実行
        List<Event> result = eventService.getAllEvents();

        //検証
        assertEquals(1, result.size());//サイズが１か
        assertEquals("testtitle", result.get(0).getTitle());//タイトル正しいか
        verify(eventRepository, times(1)).findAll();//findAll()が呼ばれたのは1回か
    }

    //IDからイベント取得テスト（成功）
    @Test
    void testGetEventById_Success() {
        //モックの設定
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        //テスト実行
        Event result = eventService.getEventById(1L);

        //検証
        assertEquals("testtitle", result.getTitle());//タイトル正しいか
        assertEquals(testUser.getId(), result.getCreator().getId());
        verify(eventRepository, times(1)).findById(1L);
    }

    //IDからイベント取得テスト（失敗）
    @Test
    void testGetEventById_NotFound() {
        //モックの設定
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        //テストの実行(assertThrows(期待するクラス, 例外を投げるはずのクラス))
        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> eventService.getEventById(999L));

        assertEquals("イベントが見つかりません。ID: 999", runtimeException.getMessage());//例外メッセージチェック
        verify(eventRepository, times(1)).findById(999L);
    }

    //イベント作成(成功)テスト
    @Test
    void testCreateEvent_Success() {
        //モックの設定
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        //テスト実行
        Event result = eventService.createEvent(testEvent);

        //検証
        assertEquals("testtitle", result.getTitle());
        assertEquals(50, result.getCapacity());
        verify(eventRepository, times(1)).save(testEvent);
    }

    //イベント作成(失敗)テスト　過去日付
    @Test
    void testCreateEvent_PastDate() {
        //過去日時を設定
        testEvent.setEventDate(LocalDateTime.now().minusDays(1));

        //テスト実行
        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> eventService.createEvent(testEvent));

        //検証
        assertEquals("イベント開催日時は未来の日時を指定してください", runtimeException.getMessage());
        verify(eventRepository, never()).save(any(Event.class));//保存処理が呼ばれないことを確認
    }

    //イベント作成(失敗)テスト　不正な定員
    @Test
    void testCreateEvent_IncorrectCapacity() {
        //不正な定員数作成
        testEvent.setCapacity(-1);

        //テスト実行
        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> eventService.createEvent(testEvent));

        //検証
        assertEquals("定員は0以上で指定してください", runtimeException.getMessage());
        verify(eventRepository, never()).save(any(Event.class));//呼ばれないことを確認
    }

    //イベント更新成功テスト
    @Test
    void testUpdateEvent_Success() {
        //更新後のイベント準備
        Event updatedEvent = new Event();
        updatedEvent.setTitle("updatedTitle");
        updatedEvent.setDescription("updatedDescription");
        updatedEvent.setEventDate(LocalDateTime.now().plusDays(10));
        updatedEvent.setCapacity(40);
        updatedEvent.setCategory(testCategory);

        //モックの設定
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));//既存のイベント取得
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);//更新処理の保存

        //テスト実行
        Event result = eventService.updateEvent(1L, updatedEvent);

        //検証
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).save(any(Event.class));
        assertEquals("updatedTitle", result.getTitle());
    }

    //イベント削除成功テスト
    @Test
    void testDeleteEvent_Success() {
        //モックの設定
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));//削除対象のイベントが存在するか

        //テスト実行
        eventService.deleteEvent(1L);

        //検証
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).delete(testEvent);
    }

    //カテゴリ指定でのイベント検索テスト
    @Test
    void testGetEventsByCategory() {
        //モックの設定
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findByCategory(testCategory)).thenReturn(events);

        //テスト実行
        List<Event> result = eventService.getEventsByCategory(testCategory);

        //検証
        assertEquals(1, result.size());
        assertEquals("testcategory", result.get(0).getTitle());//後ほど修正
        verify(eventRepository, times(0)).findByCategory(testCategory);
    }
}
