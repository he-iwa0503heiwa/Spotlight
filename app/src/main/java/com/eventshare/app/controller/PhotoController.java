package com.eventshare.app.controller;

import com.eventshare.app.service.EventService;
import com.eventshare.app.service.PhotoService;
import com.eventshare.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/photos")
@CrossOrigin("*")
public class PhotoController {
    private final PhotoService photoService;
    private final EventService eventService;
    private final UserService userService;

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

}
