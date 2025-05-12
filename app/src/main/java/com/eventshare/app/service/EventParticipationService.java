package com.eventshare.app.service;

import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.EventParticipation;
import com.eventshare.app.entity.User;

import java.util.List;

public interface EventParticipationService {
    //イベントの参加情報を取得
    List<EventParticipation> getParticipationByEvent(Event event);

    //ユーザーの参加イベント一覧を取得
    List<EventParticipation> getParticipationByUser(User user);

    //イベント参加登録
    EventParticipation participateEvent(Event event, User user);

    //参加キャンセル
    void cancelParticipation(Long participationId);

    //参加ステータス変更
    EventParticipation updateParticipationStatus(Long participationId, EventParticipation.ParticipationStatus status);

    //特定ユーザーが特定イベントに参加しているか確認
    boolean isUserParticipatingInEvent(Event event, User user);

    //特定イベントの参加人数を取得
    int getParticipantCountForEvent(Event event);
}
