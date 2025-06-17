package java.com.eventshare.app.controller;

import com.eventshare.app.dto.request.EventRequest;
import com.eventshare.app.dto.response.EventResponse;
import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.EventCategory;
import com.eventshare.app.entity.User;
import com.eventshare.app.service.EventCategoryService;
import com.eventshare.app.service.EventParticipationService;
import com.eventshare.app.service.EventService;
import com.eventshare.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/*
 イベント管理APIエンドポイントを提供するコントローラー
 1.イベント一覧取得
 2.イベント詳細取得
 3.イベント作成
 4.イベント更新
 5.イベント削除
 6.イベント検索
 */
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;
    private final EventCategoryService eventCategoryService;
    private final UserService userService;
    private final EventParticipationService eventParticipationService;

    @Autowired
    public EventController(EventService eventService,
                           EventCategoryService eventCategoryService,
                           UserService userService,
                           EventParticipationService eventParticipationService) {
        this.eventService = eventService;
        this.eventCategoryService = eventCategoryService;
        this.userService = userService;
        this.eventParticipationService = eventParticipationService;
    }

    /*
     1.イベント一覧取得API
     GET /api/events
     */
    @GetMapping
    public ResponseEntity<?> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        try {
            List<Event> events;

            // フィルタリング条件に応じてイベントを取得
            if (categoryId != null) {
                EventCategory category = eventCategoryService.getCategoryById(categoryId);
                events = eventService.getEventsByCategory(category);
            } else if (keyword != null && !keyword.trim().isEmpty()) {
                events = eventService.getEventsByKeyword(keyword);
            } else {
                events = eventService.getAllEvents();
            }

            // EventをEventResponseに変換
            List<EventResponse> eventResponses = events.stream()
                    .map(this::convertToEventResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(eventResponses);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("イベント取得中にエラーが発生しました: " + e.getMessage());
        }
    }

    /*
     2.イベント詳細取得API
     GET /api/events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id) {
        try {
            Event event = eventService.getEventById(id);
            EventResponse eventResponse = convertToEventResponse(event);
            return ResponseEntity.ok(eventResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("イベントが見つかりません: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("イベント取得中にエラーが発生しました: " + e.getMessage());
        }
    }

    /*
     3.イベント作成API
     POST /api/events
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@Valid @RequestBody EventRequest eventRequest) {
        try {
            // 現在ログインしているユーザーを取得
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User creator = userService.getUserByUsername(username);

            // カテゴリを取得
            EventCategory category = eventCategoryService.getCategoryById(eventRequest.getCategoryId());

            // リクエストDTOからEventエンティティを作成
            Event event = new Event();
            event.setTitle(eventRequest.getTitle());
            event.setDescription(eventRequest.getDescription());
            event.setEventDate(eventRequest.getEventDate());
            event.setLocation(eventRequest.getLocation());
            event.setCapacity(eventRequest.getCapacity());
            event.setCategory(category);
            event.setCreator(creator);

            // イベントを保存
            Event createdEvent = eventService.createEvent(event);

            // レスポンス用DTOに変換
            EventResponse eventResponse = convertToEventResponse(createdEvent);

            return ResponseEntity.status(HttpStatus.CREATED).body(eventResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("イベント作成に失敗しました: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("イベント作成中にエラーが発生しました: " + e.getMessage());
        }
    }

    /*
     4.イベント更新API
     PUT /api/events/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id,
                                         @Valid @RequestBody EventRequest eventRequest) {
        try {
            // 現在ログインしているユーザーを取得
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.getUserByUsername(username);

            // 更新対象のイベントを取得
            Event existingEvent = eventService.getEventById(id);

            // 作成者かどうかをチェック
            if (!existingEvent.getCreator().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("イベントの更新権限がありません");
            }

            // カテゴリを取得
            EventCategory category = eventCategoryService.getCategoryById(eventRequest.getCategoryId());

            // イベント情報を更新
            existingEvent.setTitle(eventRequest.getTitle());
            existingEvent.setDescription(eventRequest.getDescription());
            existingEvent.setEventDate(eventRequest.getEventDate());
            existingEvent.setLocation(eventRequest.getLocation());
            existingEvent.setCapacity(eventRequest.getCapacity());
            existingEvent.setCategory(category);

            // 更新を保存
            Event updatedEvent = eventService.updateEvent(id, existingEvent);

            // レスポンス用DTOに変換
            EventResponse eventResponse = convertToEventResponse(updatedEvent);

            return ResponseEntity.ok(eventResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("イベント更新に失敗しました: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("イベント更新中にエラーが発生しました: " + e.getMessage());
        }
    }

    /*
     5.イベント削除API
     DELETE /api/events/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        try {
            // 現在ログインしているユーザーを取得
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.getUserByUsername(username);

            // 削除対象のイベントを取得
            Event existingEvent = eventService.getEventById(id);

            // 作成者かどうかをチェック
            if (!existingEvent.getCreator().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("イベントの削除権限がありません");
            }

            // イベントを削除
            eventService.deleteEvent(id);

            return ResponseEntity.ok("イベントが削除されました");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("イベント削除に失敗しました: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("イベント削除中にエラーが発生しました: " + e.getMessage());
        }
    }

    /*
     6.今後のイベント検索API
     GET /api/events/upcoming
     */
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingEvents() {
        try {
            List<Event> upcomingEvents = eventService.getEventsByDateAfter(LocalDateTime.now());

            List<EventResponse> eventResponses = upcomingEvents.stream()
                    .map(this::convertToEventResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(eventResponses);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("今後のイベント取得中にエラーが発生しました: " + e.getMessage());
        }
    }

    /*
     EventエンティティをEventResponseに変換するヘルパーメソッド
     */
    private EventResponse convertToEventResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setEventDate(event.getEventDate());
        response.setLocation(event.getLocation());
        response.setCapacity(event.getCapacity());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());

        // カテゴリ情報をセット
        if (event.getCategory() != null) {
            response.setCategory(new EventResponse.CategoryInfo(
                    event.getCategory().getId(),
                    event.getCategory().getName(),
                    event.getCategory().getDescription()
            ));
        }

        // 作成者情報をセット
        if (event.getCreator() != null) {
            response.setCreator(new EventResponse.CreatorInfo(
                    event.getCreator().getId(),
                    event.getCreator().getUsername(),
                    event.getCreator().getProfilePicture()
            ));
        }

        // 参加者数をセット
        response.setParticipantCount(eventParticipationService.getParticipantCountForEvent(event));

        return response;
    }
}