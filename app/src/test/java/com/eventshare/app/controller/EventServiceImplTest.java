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

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @BeforeEach
    void setUp() {
        //テスト用ユーザー
        testUser = new User();
        testUser.setId(1l);
        testUser.setUsername("testuser");

        //テスト用カテゴリ
        testCategory = new EventCategory();
        testCategory.setId(1l);
        testCategory.setName("testcategory");

        //テスト用イベント
        testEvent = new Event();
        testEvent.setId(1l);
        testEvent.setTitle("testtitle");
        testEvent.setDescription("testdescription");
        testEvent.setEventDate(LocalDateTime.now().plusDays(7));
        testEvent.setCapacity(50);
        testEvent.setCategory(testCategory);
        testEvent.setCreator(testUser);
    }

    @Test
    void testGetAllEvents() {
        //モックの設定
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findAll()).thenReturn(events);

        //テスト実行
        List<Event> result = eventService.getAllEvents();

        //検証
        assertEquals(1, result.size());//サイズが１か
        assertEquals("", result.get(0).getTitle());//タイトル正しいか
        verify(eventRepository, times(1)).findAll();//findAll()が呼ばれたのは1回か
    }
}
