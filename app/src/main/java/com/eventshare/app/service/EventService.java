package com.eventshare.app.service;

import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.EventCategory;
import com.eventshare.app.entity.User;

import java.util.List;
import java.time.LocalDateTime;

public interface EventService {
    //すべてのイベントを取得
    List<Event> getAllEvents();

    //IDによるイベント検索
    Event getEventById(Long id);

    //イベント作成
    Event createEvent(Event event);

    //イベント更新
    Event updateEvent(Long id, Event event);

    //イベント削除
    void deleteEvent(Long id);

    //カテゴリによるイベント検索
    List<Event> getEventsByCategory(EventCategory category);

    //作成者によるイベント検索
    List<Event> getEventsByCreator(User creator);

    //日付によるイベント検索
    List<Event> getEventsByDateAfter(LocalDateTime date);

    //キーワードによるイベント検索
    List<Event> getEventsByKeyword(String keyword);

    //カテゴリと日付による検索
    List<Event> getEventsByCategoryAndDateAfter(EventCategory category, LocalDateTime date);
}