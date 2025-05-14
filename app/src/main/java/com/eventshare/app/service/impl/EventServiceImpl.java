package com.eventshare.app.service.impl;

import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.EventCategory;
import com.eventshare.app.entity.User;
import com.eventshare.app.repository.EventRepository;
import com.eventshare.app.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class EventServiceImpl implements EventService {
    //リポジトリの依存性注入
    private final EventRepository eventRepository;

    //コンストラクタインジェクション
    @Autowired
    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("イベントが見つかりません。ID: " + id));
    }

    @Override
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public Event updateEvent(Long id, Event event) {
        //更新対象のイベント取得
        Event updatingEvent = getEventById(id);
        //イベント更新
        updatingEvent.setTitle(event.getTitle());
        updatingEvent.setDescription(event.getDescription());
        updatingEvent.setEventDate(event.getEventDate());
        updatingEvent.setLocation(event.getLocation());

        return eventRepository.save(updatingEvent);
    }

    @Override
    public void deleteEvent(Long id) {
        Event event = getEventById(id);
        eventRepository.delete(event);
    }

    @Override
    public List<Event> getEventsByCategory(EventCategory category) {
        return eventRepository.findByCategory(category);
    }

    @Override
    public List<Event> getEventsByCreator(User creator) {
        return eventRepository.findByCreator(creator);
    }

    @Override
    public List<Event> getEventsByDateAfter(LocalDateTime date) {
        return eventRepository.findByEventDateAfter(date);
    }

    @Override
    public List<Event> getEventsByKeyword(String keyword) {
        return eventRepository.findByTitleContaining(keyword);
    }

    @Override
    public List<Event> getEventsByCategoryAndDateAfter(EventCategory category, LocalDateTime date) {
        return eventRepository.findByCategoryAndEventDateAfter(category, date);
    }
}