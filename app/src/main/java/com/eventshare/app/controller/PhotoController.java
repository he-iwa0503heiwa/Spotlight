package com.eventshare.app.controller;

import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.Photo;
import com.eventshare.app.entity.User;
import com.eventshare.app.service.EventService;
import com.eventshare.app.service.PhotoService;
import com.eventshare.app.service.UserService;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/photos")
@CrossOrigin(origins = "*")
public class PhotoController {
    private final PhotoService photoService;
    private final EventService eventService;
    private final UserService userService;
    private final Tika tika = new Tika();

    @Autowired
    public PhotoController(PhotoService photoService, EventService eventService, UserService userService){
        this.photoService = photoService;
        this.eventService = eventService;
        this.userService = userService;
    }

    /*
    1.写真をアップロード
    POST /api/photos/upload/{eventID}
     */
    @PostMapping("/upload/{eventID}")
    public ResponseEntity<?> uploadPhoto(
            @PathVariable Long eventId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption
            ){
        try {
            //ユーザー取得
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username);

            //イベント取得
            Event event = eventService.getEventById(eventId);

            //写真をアップロード
            Photo photo = photoService.uploadPhoto(file, caption, event, user);

            //レスポンス用DTO
            PhotoResponse response = convertToPhotoResponse(photo);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            //400エラー
            return ResponseEntity.badRequest().body("写真のアップロードに失敗しました: " + e.getMessage());
        }catch (Exception e){
            //500エラー
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("写真のアップロード中にエラーが発生しました: " + e.getMessage());
        }
    }
    /*
     2. イベントの写真一覧取得API
     GET /api/photos/event/{eventId}
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getPhotosByEvent(@PathVariable Long eventId){
        try{
            Event event = eventService.getEventById(eventId);

            //イベントの写真一覧取得
            List<Photo> photos = photoService.getPhotosByEvent(event);

            //レスポンス用のDTOを作成
            List<PhotoResponse> responses = photos.stream()//List<Photo> → Stream<Photo>
                    .map(this::convertToPhotoResponse)//Photo → PhotoResponse (一個ずつ)
                    .collect(Collectors.toList());//Stream<PhotoResponse> → List<PhotoResponse>

            return ResponseEntity.ok(responses);
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body("イベントの写真一覧を取得できませんでした: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("写真一覧取得中にエラーが発生しました: " + e.getMessage());
        }
    }

    /*
     3. 写真ファイル取得API
     GET /api/photos/file/{filename}
     */
    @GetMapping("/file/{filename}")
    public ResponseEntity<byte[]> getPhotoFile(@PathVariable String filename){
        try {
            //ファイルデータの取得
            byte[] photoData = photoService.getPhotoFile(filename);

            //TikaでContent-Typeを推測
            String contentType = detectContentType(photoData, filename);

            //画像かどうかチェック
            if (!contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().build();
            }

            //ヘッダー情報を設定
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", contentType);//タイプ設定
            headers.set("Content-Length", String.valueOf(photoData.length));//サイズ設定
            headers.setCacheControl("public, max-age=3600");//1時間キャッシュ

            return new ResponseEntity<>(photoData, headers, HttpStatus.OK);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();//メッセージなし
        }
    }

    /*
     4. 写真削除API
     DELETE /api/photos/{photoId}
     */
    @DeleteMapping("/{photoId}")
    public ResponseEntity<?> deletePhoto(@PathVariable Long photoId) {
        try {
            //ユーザー取得
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username);

            //写真を削除
            photoService.deletePhoto(photoId, user);

            return ResponseEntity.ok("写真が削除されました");
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body("写真の削除に失敗しました: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("写真削除中にエラーが発生しました: " + e.getMessage());
        }
    }

    /*
    PhotoエンティティをPhotoResponseに変換
    */
    private PhotoResponse convertToPhotoResponse(Photo photo) {

        PhotoResponse response = new PhotoResponse();  //新しいレスポンスオブジェクトを作成
        response.setId(photo.getId());
        response.setFilename(photo.getFilename());
        response.setOriginalFilename(photo.getUploadFilename());
        response.setCaption(photo.getCaption());
        response.setFileSize(photo.getFileSize());
        response.setContentType(photo.getMineType());
        response.setUploadedAt(photo.getUploadedAt());
        response.setEventId(photo.getEvent().getId());

        //アップロード者の情報を設定
        UploaderInfo uploaderInfo = new UploaderInfo();
        uploaderInfo.setId(photo.getUploadedBy().getId());
        uploaderInfo.setUsername(photo.getUploadedBy().getUsername());
        response.setUploadedBy(uploaderInfo);

        return response;  //画面表示用の安全なオブジェクトを返す
    }

    /*
    写真情報レスポンス用のDTO
     */
    public static class PhotoResponse{
        private Long id;
        private String filename;
        private String originalFilename;
        private String caption;
        private Long fileSize;
        private String contentType;
        private LocalDateTime uploadedAt;
        private Long eventId;  //Eventエンティティ全体ではなく、IDのみ
        private UploaderInfo uploadedBy;  //Userエンティティ全体ではなく、必要な情報のみ

        //GettersとSetters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }

        public String getOriginalFilename() { return originalFilename; }
        public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

        public String getCaption() { return caption; }
        public void setCaption(String caption) { this.caption = caption; }

        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public LocalDateTime getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

        public Long getEventId() { return eventId; }
        public void setEventId(Long eventId) { this.eventId = eventId; }

        public UploaderInfo getUploadedBy() { return uploadedBy; }
        public void setUploadedBy(UploaderInfo uploadedBy) { this.uploadedBy = uploadedBy; }
    }

    /*
    アップロード者情報用の内部クラス
    */
    public static class UploaderInfo {
        private Long id;
        private String username;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    /*
    Tikaでファイル形式を判定
     */
    private String detectContentType(byte[] data, String filename){
        try {
            return tika.detect(data, filename);
        } catch (Exception e) {
            return fallbackDetection(filename);
        }
    }

    /*
    フォールバック（予備プラン）
     */
    private String fallbackDetection(String filename) {
        if (filename == null) return "application/octet-stream";//よくわからないファイルとして返す

        String lowerCase = filename.toLowerCase();//小文字にする
        if (lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg")) return "image/jpeg";
        if (lowerCase.endsWith(".png")) return "image/png";
        if (lowerCase.endsWith(".gif")) return "image/gif";
        if (lowerCase.endsWith(".webp")) return "image/webp";

        return "application/octet-stream";
    }
}
