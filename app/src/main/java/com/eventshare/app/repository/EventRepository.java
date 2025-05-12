package com.eventshare.app.repository;

import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.EventCategory;
import com.eventshare.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    //カテゴリによるイベント検索
    List<Event> findByCategory(EventCategory category);

    //作成者によるイベント検索
    List<Event> findByCreator(User creator);

    //イベント日時が指定日以降のイベント検索
    List<Event> findByEventDateAfter(LocalDateTime date);

    //タイトルに特定のキーワードを含むイベント検索
    List<Event> findByTitleContaining(String keyword);

    //カテゴリと日付による検索
    List<Event> findByCategoryAndEventDateAfter(EventCategory category, LocalDateTime date);
}