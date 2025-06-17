package java.com.eventshare.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.com.eventshare.app.dto.response.EventParticipationResponse;

/*
 イベント参加関連のAPIエンドポイントを提供するコントローラー
 1. イベント参加登録
 2. 参加キャンセル
 3. 参加状況確認
 4. イベントの参加者一覧取得
 */
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventParticipationController {
    private final com.eventshare.app.service.EventService eventService;
    private final com.eventshare.app.service.EventParticipationService eventParticipationService;
    private final com.eventshare.app.service.UserService userService;

    @Autowired
    public EventParticipationController(com.eventshare.app.service.EventService eventService,
                                        com.eventshare.app.service.EventParticipationService eventParticipationService,
                                        com.eventshare.app.service.UserService userService) {
        this.eventService = eventService;
        this.eventParticipationService = eventParticipationService;
        this.userService = userService;
    }

    /*
     1. イベント参加登録API
     POST /api/events/{id}/participate
    */
    @PostMapping("/{id}/participate")
    public ResponceEntity<?> participateEvent(@PathVariable Long id) {
        try {
            //現在ログインしているユーザーを取得
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            com.eventshare.app.entity.User user = userService.getUserByUsername(username);

            //イベントを取得
            com.eventshare.app.entity.Event event = eventService.getEventById(id);

            //既に参加しているかチェック
            if (eventParticipationService.isUserParticipatingInEvent(event, user)) {
                return ResponseEntity.badRequest().body("既にこのイベントに参加しています");
            }

            //参加登録
            com.eventshare.app.entity.EventParticipation participation = eventParticipationService.participateEvent(event, user);

            //レスポンス用DTOに変換
            EventParticipationResponse response = convertToParticipationResponse(participation);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("イベント参加登録に失敗しました: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("イベント参加登録中にエラーが発生しました: " + e.getMessage());
        }
    }
}