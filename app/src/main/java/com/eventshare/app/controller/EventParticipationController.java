package com.eventshare.app.controller;

import com.eventshare.app.dto.response.EventParticipationResponse;
import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.EventParticipation;
import com.eventshare.app.entity.User;
import com.eventshare.app.service.EventParticipationService;
import com.eventshare.app.service.EventService;
import com.eventshare.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/*
 イベント参加関連のAPIエンドポイントを提供するコントローラー
 1.イベント参加登録
 2.参加キャンセル
 3.参加状況確認
 4.イベントの参加者一覧取得
 5.ユーザーの参加イベント一覧取得
 */
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")//cors設定
public class EventParticipationController {
    private final EventService eventService;
    private final EventParticipationService eventParticipationService;
    private final UserService userService;

    @Autowired
    public EventParticipationController(EventService eventService,
                                        EventParticipationService eventParticipationService,
                                        UserService userService) {
        this.eventService = eventService;
        this.eventParticipationService = eventParticipationService;
        this.userService = userService;
    }

    /*
     1.イベント参加登録API
     POST /api/events/{id}/participate
    */
    @PostMapping("/{id}/participate")
    public ResponseEntity<?> participateEvent(@PathVariable Long id) {
        try {
            //セキュリティコンテキスからログイン中のユーザー名取得
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username);//ユーザー名を利用してuserServiceからユーザー情報取得

            //イベントを取得
            Event event = eventService.getEventById(id);

            //既に参加しているかチェック
            if (eventParticipationService.isUserParticipatingInEvent(event, user)) {
                return ResponseEntity.badRequest().body("既にこのイベントに参加しています");
            }

            //参加登録
            EventParticipation participation = eventParticipationService.participateEvent(event, user);

            //レスポンス用DTOに変換
            EventParticipationResponse response = convertToParticipationResponse(participation);

            //登録成功で201を返す。
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            //ビジネスロジック上の400エラー
            return ResponseEntity.badRequest().body("イベント参加登録に失敗しました: " + e.getMessage());
        } catch (Exception e) {
            //予期しないエラー500エラー
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("イベント参加登録中にエラーが発生しました: " + e.getMessage());
        }
    }

    /*
    2. 参加キャンセルAPI
    DELETE /api/events/{id}/participate
    */
    @DeleteMapping("/{id}/participate")
    public ResponseEntity<?> cancelParticipation(@PathVariable Long id){
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username);

            Event event = eventService.getEventById(id);

            //参加してるかチェック
            if (!eventParticipationService.isUserParticipatingInEvent(event, user)){
                return ResponseEntity.badRequest().body("このイベントには参加してません");
            }

            //参加記録を取得して削除
            List<EventParticipation> participations = eventParticipationService.getParticipationByEventAndUser(event, user);
            if (!participations.isEmpty()) {
                EventParticipation participation = participations.get(0);//リストの最初の要素を取得
                eventParticipationService.cancelParticipation(participation.getId());//参加記録のidでサービス層でキャンセル
            }

            //成功時は200ok
            return ResponseEntity.ok("イベント参加をキャンセルしました");

        } catch (RuntimeException e) {//例外処理(参加apiと同じ)
            return ResponseEntity.badRequest().body("参加キャンセルに失敗しました: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("参加キャンセル中にエラーが発生しました: " + e.getMessage());
        }
    }
    /*
     EventParticipationエンティティをEventParticipationResponseに変換するヘルパーメソッド
     */
    private EventParticipationResponse convertToParticipationResponse(com.eventshare.app.entity.EventParticipation participation) {
        EventParticipationResponse response = new EventParticipationResponse();
        response.setId(participation.getId());
        response.setEventId(participation.getEvent().getId());
        response.setEventTitle(participation.getEvent().getTitle());
        response.setUserId(participation.getUser().getId());
        response.setUsername(participation.getUser().getUsername());
        response.setParticipatedAt(participation.getCreatedAt());
        return response;
    }
}