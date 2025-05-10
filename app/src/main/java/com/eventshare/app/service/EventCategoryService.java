package com.eventshare.app.service;

import com.event.app.entity.EventCategory;

import java.util.List;

public interface EventCategoryService {
    //すべてのイベントカテゴリを取得
    List<EventCategory> getAllCategories();

    //指定されたIDのカテゴリを取得
    EventCategory getCategoryById(Long id);

    //指定された名前のカテゴリを取得
    EventCategory getCategoryByName(String name);

    //新しいカテゴリを作成
    EventCategory createCategory(EventCategory category);

    //既存のカテゴリを更新する
    EventCategory updateCategory(Long id, EventCategory category);

    //カテゴリを削除
    void deleteCategory(Long id);

    //指定された名前のカテゴリが存在するか確認
    boolean existsByName(String name);
}