package com.eventshare.app.repository;

import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.Photo;
import com.eventshare.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    //イベントIDで写真一覧を降順で取得
    List<Photo> findByEventOrderByUploadedAtDesc(Event event);

    //特定のユーザーがアップロードした写真一覧を降順で取得
    List<Photo> findByUploadByOrderByUploadedAtDesc(User user);

    //イベントの写真数を取得（カスタムクエリ）
    @Query("SELECT COUNT(p) FROM Photo p WHERE p.event = :event")
    long countByEvent(@Param("event") Event event);

    //特定のイベントで特定のユーザーがアップロードした写真を取得
    List<Photo> findByEventAndUploadByOrderByUploadedAtDesc(Event event, User user);
}
