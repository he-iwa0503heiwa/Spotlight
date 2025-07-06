package com.eventshare.app.service.impl;

import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.Photo;
import com.eventshare.app.entity.User;
import com.eventshare.app.repository.PhotoRepository;
import com.eventshare.app.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class PhotoServiceImpl implements PhotoService {
    //リポジトリの依存性の注入
    private final PhotoRepository photoRepository;

    @Value("${file.upload.directory:uploads/photos}")
    private String uploadDirectory;

    //コンストラクタインジェクション
    @Autowired
    public PhotoServiceImpl(PhotoRepository photoRepository){
        this.photoRepository = photoRepository;
    }

    /*
    以下メソッド実装
     */
    //写真を保存するメソッド
    @Override
    public Photo uploadPhoto(MultipartFile file, String caption, Event event, User user){
        try {
            //バリデーションチェック
            validateFile(file);

            //アップロード先のディレクトリを準備
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)){//ファイルディレクトリが存在しない場合
                Files.createDirectories(uploadPath);
            }

            //ファイル名生成
            String originalFilename = file.getOriginalFilename();//元のファイル名
            String fileExtension = getFileExtension(originalFilename);
            String filename = UUID.randomUUID().toString() + fileExtension;

            //ファイルをディスクに保存
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            //データベースに写真情報を保存
            Photo photo = new Photo();
            photo.setFilename(filename);
            photo.setOriginalFilename(originalFilename);
            photo.setCaption(caption);
            photo.setFileSize(file.getSize());
            photo.setContentType(file.getContentType());
            photo.setEvent(event);
            photo.setUploadedBy(user);

            return photoRepository.save(photo);

        } catch (IOException e){
            throw new RuntimeException("ファイルの保存に失敗しました: " + e.getMessage());
        }
    }

    //イベントから写真を取得
    @Override
    public List<Photo> getPhotosByEvent(Event event){
        return photoRepository.findByEventOrderByUploadedAtDesc(event);
    }

    //写真を削除
    @Override
    public void deletePhoto(Long photoId, User user) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("写真が見つかりません"));

        //投稿者本人かイベント作成者のみ削除可能
        if (!photo.getUploadedBy().getId().equals(user.getId()) &&
        !photo.getEvent().getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("写真を削除する権限がありません");
        }

        try {
            //ファイルを削除
            Path filePath = Paths.get(uploadDirectory, photo.getFilename());
            Files.deleteIfExists(filePath);

            //データベースから削除
            photoRepository.delete(photo);
        } catch (IOException e){
            throw new RuntimeException("ファイルの削除に失敗しました: " + e.getMessage());
        }
    }

    @Override
    public byte[] getPhotoFile(String filename) {
        try {
            Path filePath = Paths.get(uploadDirectory, filename);
            if (!Files.exists(filePath)) {
                throw new RuntimeException("ファイルが見つかりません: " + filename);
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("ファイルの読み込みに失敗しました: " + e.getMessage());
        }
    }

    @Override
    public Photo getPhotoById(Long photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("写真が見つかりません"));
    }

    @Override
    public List<Photo> getPhotosByUser(User user) {
        return photoRepository.findByUploadedByOrderByUploadedAtDesc(user);
    }

    @Override
    public long getPhotoCountByEvent(Event event) {
        return photoRepository.countByEvent(event);
    }

    @Override
    public List<Photo> getPhotosByEventAndUser(Event event, User user) {
        return photoRepository.findByEventAndUploadedByOrderByUploadedAtDesc(event, user);
    }

    /*
    以下プライベートメソッド
    実装の詳細
     */
    //バリデーションチェックの関数
    private void validateFile(MultipartFile file){
        //空じゃないか
        if (file.isEmpty()){
            throw new RuntimeException("ファイルが選択されていません");
        }

        //ファイルサイズチェック(10MB以下)
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("ファイルサイズは10MB以下にしてください");
        }

        //ファイル形式
        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)){
            throw new RuntimeException("ファイルの形式が不適切です。PEG、PNG、GIF、WebPのみアップロード可能です");
        }
    }

    //ファイル形式が有効かチェックする関数
    private boolean isValidImageType(String contentType){
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp");
    }

    //ファイルの拡張子を取得する関数
    private String getFileExtension(String filename){
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
