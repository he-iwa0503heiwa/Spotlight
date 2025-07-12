package com.eventshare.app.service;

import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.Photo;
import com.eventshare.app.entity.User;
import org.hibernate.boot.archive.scan.internal.ScanResultImpl;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhotoService {

    //アップロードされたファイル,写真の説明,写真を投稿するイベント,写真を投稿するユーザー
    //写真をアップロードして保存
    Photo uploadPhoto(MultipartFile file, String caption, Event event, User user);

    //指定されたイベントの写真一覧を取得（新しい順)
    List<Photo> getPhotosByEvent(Event event);

    //写真を削除
    void deletePhoto(Long photoId, User user);

    //写真ファイルのバイナリデータを取得
    byte[] getPhotoFile(String filename);

    //写真をIDで取得
    Photo getPhotoById(Long photoId);

    //特定のユーザーがアップロードした写真を取得
    List<Photo> getPhotosByUser(User user);

    //特定のイベントの写真の数
    long getPhotoCountByEvent(Event event);

    //特定のユーザーがアップロードした特定のイベントの写真
    List<Photo> getPhotosByEventAndUser(Event event, User user);
}
