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
 3.イベントの参加者一覧取得
 4.ユーザーの参加イベント一覧取得
 5.参加状況確認
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

            //URLパラメータのIDからイベント情報を取得
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
            //セキュリティコンテキスからログイン中のユーザー名取得
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username);//ユーザー名を利用してuserServiceからユーザー情報取得

            //URLパラメータのIDからイベント情報を取得
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
    3. イベント参加者一覧取得API
    GET /api/events/{id}/participants
    */
    @GetMapping("/{id}/participants")
    public ResponseEntity<?> getEventParticipants(@PathVariable Long id){
        try {
            //idからイベント取得
            Event event = eventService.getEventById(id);
            //イベントから参加者取得
            List<EventParticipation> participations = eventParticipationService.getParticipationByEvent(event);
            //レスポンス用意にDTOに変換
            List<EventParticipationResponse> responses = participations.stream()//各要素へのストリーム処理
                    .map(this::convertToParticipationResponse)//各参加記録をレスポンスDTO用に変換
                    .collect(Collectors.toList());//リストに集約
            return ResponseEntity.ok(responses);//okを返還

        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body("参加者一覧の取得に失敗しました");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("参加者一覧取得中にエラーが発生しました: " + e.getMessage());
        }
    }

    /*
    4. ユーザーの参加イベント一覧取得API
    GET /api/events/my-participations
    */
    @GetMapping("/my-participations")
    public ResponseEntity<?> getMyparticipations(){
        try {
            //セキュリティコンテキスからログイン中のユーザー名取得
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username);//ユーザー名を利用してuserServiceからユーザー情報取得

            //特定のイベントに参加している全ユーザーを取得
            List<EventParticipation> participations = eventParticipationService.getParticipationByUser(user);
            //レスポンス用意にDTOに変換
            List<EventParticipationResponse> responses = participations.stream()//各要素へのストリーム処理
                    .map(this::convertToParticipationResponse)//各参加記録をレスポンスDTO用に変換
                    .collect(Collectors.toList());//リストに集約
            return ResponseEntity.ok(responses);//okを返還

        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body("参加イベント一覧の取得に失敗しました");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("参加イベント一覧取得中にエラーが発生しました: " + e.getMessage());
        }
    }

    /*
    5. 参加状況確認API
    GET /api/events/{id}/participation-status
    */
    @GetMapping("/{id}/participation-status")
    public ResponseEntity<?> getParticipationStatus(@PathVariable Long id){
        try {
            //現在ログインしているユーザーを取得
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username);

            //URLパラメータのIDからイベント情報を取得
            Event event = eventService.getEventById(id);

            //ログイン中のユーザーがパラメータのイベントに参加しているかどうか
            boolean isPaticipating = eventParticipationService.isUserParticipatingInEvent(event, user);
            //okと参加状況を内部クラスのオブジェクトとして返す
            return ResponseEntity.ok(new ParticipationStatusResponse(isPaticipating));

        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body("参加状況の取得に失敗しました");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("参加状況取得中にエラーが発生しました: " + e.getMessage());
        }
    }

    /*
     EventParticipationエンティティをEventParticipationResponseに変換するヘルパーメソッド
     */
    //プライベートメソッドでエンティティからDTOへ
    private EventParticipationResponse convertToParticipationResponse(EventParticipation participation) {
        //空のレスポンスオブジェクト生成
        EventParticipationResponse response = new EventParticipationResponse();
        response.setId(participation.getId());
        response.setEventId(participation.getEvent().getId());
        response.setEventTitle(participation.getEvent().getTitle());
        response.setUserId(participation.getUser().getId());
        response.setUsername(participation.getUser().getUsername());
        response.setStatus(participation.getStatus().toString());
        response.setParticipatedAt(participation.getCreatedAt());
        return response;
    }
    /*
    参加状況レスポンス用の内部クラス
     */
    public static class ParticipationStatusResponse{
        private boolean participating;

        //コンストラクタ
        public ParticipationStatusResponse(boolean participating){
            this.participating = participating;
        }

        //getter
        public boolean isPaticipating(){
            return participating;
        }

        //setter
        public void setPaticipating(boolean paticipating){
            this.participating = paticipating;
        }
    }
}