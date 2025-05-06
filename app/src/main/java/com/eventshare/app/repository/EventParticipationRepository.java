package com.eventshare.app.repository;

import com.eventshare.app.entity.EventParticipation;
import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface EventParticipationRepository extends JpaRepository<EventParticipation, Long> {
    //特定のイベントの参加者リスト取得
    List<EventParticipation> findByEvent(Event event);

    //特定のユーザーが参加しているイベントリスト取得
    List<EventParticipation> findByUser(User user);

    //特定のユーザーと特定のイベントの参加情報を取得
    List<EventParticipation> findByEventAndUser(Event event, User user);

    //特定のユーザーが特定のイベントに参加しているか確認
    boolean existsByEventAndUser(Event event, User user);

    //特定のステータスの参加情報を取得
    List<EventParticipation> findByEventAndStatus(Event event, EventParticipation.ParticipationStatus status);
}