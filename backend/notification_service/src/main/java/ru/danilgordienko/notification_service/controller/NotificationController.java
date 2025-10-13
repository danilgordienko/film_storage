package ru.danilgordienko.notification_service.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.danilgordienko.notification_service.model.dto.request.NotificationRequestDto;
import ru.danilgordienko.notification_service.model.dto.response.NotificationListResponseDto;
import ru.danilgordienko.notification_service.model.dto.response.NotificationResponseDto;
import ru.danilgordienko.notification_service.model.dto.response.NotificationsInfoDto;
import ru.danilgordienko.notification_service.model.enums.Type;
import ru.danilgordienko.notification_service.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("api/notifications")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    private final NotificationService  notificationService;

    @GetMapping
    public ResponseEntity<NotificationListResponseDto> getAllNotifications(
            @RequestParam Long id
    ) {
        log.info("GET api/notifications - Request to get all notifications by user id {}", id);
        var response  = notificationService.getAllNotifications(id);
        log.info("GET api/notifications - Get all notifications by user id {}, count: {}",id, response.getCount());
        return  ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<NotificationsInfoDto> getNotificationsInfo(
            @RequestParam Long id
    ) {
        log.info("GET api/notifications/info - Request to get notifications info by user id {}", id);
        var response  = notificationService.getNotificationsInfo(id);
        log.info("GET api/notifications/info - Get notifications info by user id {}", id);
        return  ResponseEntity.ok(response);
    }

    @PostMapping("/read")
    public ResponseEntity<String> markNotificationsAsRead(
            @RequestParam Long id
    ) {
        log.info("GET api/notifications/read - Request to mark notifications as read by user id {}", id);
        notificationService.markNotificationsAsRead(id);
        log.info("GET api/notifications/read - Successfully mark notifications as read by user id {}", id);
        return  ResponseEntity.ok().build();
    }



}
