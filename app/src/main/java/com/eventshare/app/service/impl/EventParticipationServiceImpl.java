package com.eventshare.app.service.impl;

import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.EventParticipation;
import com.eventshare.app.entity.User;
import com.eventshare.app.repository.EventParticipationRepository;
import com.eventshare.app.service.EventParticipationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventParticipationServiceImpl implements EventParticipationService {
    //リポジトリの依存性注入
    private final EventParticipationRepository eventParticipationRepository;

    //コンストラクタインジェクション
    @Autowired
    public EventParticipationServiceImpl(EventParticipationRepository eventParticipationRepository) {
        this.eventParticipationRepository = eventParticipationRepository;
    }

    //以下メソッド実装
    @Override
    public List<EventParticipation> getParticipationByEvent(Event event) {
        return eventParticipationRepository.findByEvent(event);
    }

    @Override
    public List<EventParticipation> getParticipationByUser(User user) {
        return eventParticipationRepository.findByUser(user);
    }

    @Override
    public EventParticipation participateEvent(Event event, User user) {
        if (eventParticipationRepository.existsByEventAndUser(Event event, User user)) {
            throw new RuntimeException("このイベントにはすでに参加しています");
        }
        //定員確認（定員が設定されている場合）
        if (event.getCapacity() != null) {
            //このイベントでの現在の参加者数を取得
            int currentParticipants = getParticipantCountForEvent(event);
            //定員オーバー（キャンセル待ち）：イベント情報とユーザー情報とステータスをセット
            if (currentParticipants >= event.getCapacity()) {
                EventParticipation participation = new EventParticipation();
                participation.setEvent(event);
                participation.setStatus(EventParticipation.ParticipationStatus.WAITING);
                participation.setUser(user);
                return eventParticipationRepository.save(participation);
            }
            //通常登録：イベント情報とユーザー情報とステータスをセット
            EventParticipation participation = new EventParticipation();
            participation.setEvent(event);
            participation.setStatus(EventParticipation.ParticipationStatus.CONFIRMED);
            participation.setUser(user);
            return eventParticipationRepository.save(participation);
        }
    }

    @Override
    public void cancelParticipation(Long participationId) {
        EventParticipation participation = eventParticipationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("参加情報が見つかりません。ID：　" + participationId));
        //ステータスをキャンセルに変更する
        participation.setStatus(EventParticipation.ParticipationStatus.CANCELLED);
        eventParticipationRepository.save(participation);
    }

    @Override
    public EventParticipation updateParticipationStatus(Long participationId, EventParticipation.ParticipationStatus status) {
        EventParticipation participation = eventParticipationRepository.findById(participationId)
                .orElseThow(() -> new RuntimeException("参加情報が見つかりません。ID：　" + participationId));
        //ステータスを変更する
        participation.setStatus(status);
        return eventParticipationRepository.save(participation);
    }

    @Override
    public boolean isUserParticipatingInEvent(Event event, User user){
        return eventParticipationRepository.existsByEventAndUser(Event event, User user);
    }

    @Override
    public int getParticipantCountForEvent(Event event){
        List<EventParticipation> comfirmdParticipations = eventParticipationRepository.findByEventAndStatus(Event event, EventParticipation.ParticipationStatus status);
        return comfirmdParticipations.size();
    }
}