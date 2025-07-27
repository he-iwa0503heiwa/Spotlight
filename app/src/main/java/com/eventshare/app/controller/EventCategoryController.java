package com.eventshare.app.controller;

import com.eventshare.app.entity.Event;
import com.eventshare.app.entity.EventCategory;
import com.eventshare.app.service.EventCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class EventCategoryController {
    private final EventCategoryService eventCategoryService;

    @Autowired
    public EventCategoryController(EventCategoryService eventCategoryService){
        this.eventCategoryService = eventCategoryService;
    }

    /*
    カテゴリ一覧取得
    GET /api/categories
     */
    @GetMapping
    public ResponseEntity<List<EventCategory>> getAllCategories() {
        try {
            List<EventCategory> categories = eventCategoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /*
    カテゴリ詳細取得
    GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventCategory> getCategoryById(@PathVariable Long id) {
        try {
            EventCategory category = eventCategoryService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
