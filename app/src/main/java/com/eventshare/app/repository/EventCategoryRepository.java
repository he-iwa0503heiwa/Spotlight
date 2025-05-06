package com.eventshare.app.repository;

import com.eventshare.app.entity.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface EventCategoryRepository extends JpaRepository<EventCategory, Long> {
    //カテゴリ名で詳細検索
    Optional<EventCategory> findByName(String name);

    //カテゴリ名存在チェック
    boolean existsByName(String name);

    //名前で並べ替えてすべてのカテゴリを取得
    List<EventCategory> findAllByOrderBynameAsc();

    //キーワードを含むカテゴリを検索
    List<EventCategory> findByNameContaining(String keyword);
}