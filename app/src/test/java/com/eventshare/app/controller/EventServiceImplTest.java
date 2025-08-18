package com.eventshare.app.controller;

import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.EventCategory;
import com.eventshare.app.entity.User;
import com.eventshare.app.repository.EventRepository;
import com.eventshare.app.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

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
}
