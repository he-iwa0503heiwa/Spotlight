package com.eventshare.app.service.impl;

import com.eventshare.app.entity.EventCategory;
import com.eventshare.app.repository.EventCategoryRepository;
import com.eventshare.app.service.EventCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventCategoryServiceImpl implements EventCategoryService {
    //リポジトリの依存性の注入
    private final EventCategoryRepository eventCategoryRepository;

    //コンストラクタインジェクション
    @Autowired
    public EventCategoryServiceImpl(EventCategoryRepository eventCategoryRepository) {
        this.eventCategoryRepository = eventCategoryRepository;
    }

    //以下メソッド実装
    @Override
    public List<EventCategory> getAllCategories() {
        return eventCategoryRepository.findAll();
    }

    @Override
    public EventCategory getCategoryById(Long id) {
        return eventCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("カテゴリが見つかりません。ID: " + id));
    }

    @Override
    public EventCategory getCategoryByName(String name) {
        return eventCategoryRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("カテゴリが見つかりません。名前: " + name));
    }

    @Override
    public EventCategory createCategory(EventCategory category) {
        if (eventCategoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("そのカテゴリ名はすでに存在します：　" + category.getName());
        }
        return eventCategoryRepository.save(category);
    }

    @Override
    public EventCategory updateCategory(Long id, EventCategory category) {
        //更新対象を取得
        EventCategory existingCategory = getCategoryById(id);

        //「変更がある」かつ「すでに存在している」場合は例外をスロー
        if (!existingCategory.getName().equals(category.getName()) &&
                eventCategoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("そのカテゴリ名はすでに存在します：　" + category.getName());
        }
        //更新情報をセット
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        return eventCategoryRepository.save(existingCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        //更新対象を取得
        EventCategory category = getCategoryById(id);

        if (!category.getEvents().isEmpty()) {
            throw new RuntimeException("このカテゴリには関連するイベントがあるため削除できません");
        }
        eventCategoryRepository.delete(category);
    }

    @Override
    public boolean existsByName(String name) {
        return eventCategoryRepository.existsByName(name);
    }
}