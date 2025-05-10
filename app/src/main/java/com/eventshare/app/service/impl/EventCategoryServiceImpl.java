package com.eventshare.app.service;

import com.event.app.entity.EventCategory;
import com.event.app.repository.EventCategoryRepository;
import com.event.app.service.EventCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventCategoryServiceImpl implements EventCategoryService {
    //リポジトリの依存性の注入
    private final EventCategoryRepository EventCategoryRepository;

    @Autowired
    public EventCategoryServiceImpl(EventCategoryRepository eventCategoryRepository) {
        this.eventCategoryRepository = eventCategoryRepository;
    }

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
                .orElseThrow(() -> new RuntimeExcexption("カテゴリが見つかりません。名前: " + name));
    }

    @Override
    public EventCategory createCategory(EventCategory category) {
        if (eventCategoryRepository.existsByName(category.findByName())) {
            throw new RuntimeExcexption("そのカテゴリ名はすでに存在します：　" + category.findByName();
        }
        return eventCategoryRepository.save(category)
    }

    @Override
    public EventCategory updateCategory(Long id, EventCategory category) {
        //更新対象を取得
        EventCategory existingCategory = getCategoryById(id);

        //「変更がある」かつ「すでに存在している」場合エラー
        if (!existingCategory.getName().equals(category.getName()) && eventCategoryRepository.existsByName(category.getName())) {
            throw new RuntimeExcexption("そのカテゴリ名はすでに存在します：　" + category.findByName();
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

        if (!category.getEvent().isEmpty()) {
            throw new RuntimeExcexption("このカテゴリには関連するイベントがあるため削除できません");
        }
        eventCategoryRepository.delete(category);
    }

}