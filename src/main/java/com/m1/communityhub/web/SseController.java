package com.m1.communityhub.web;

import com.m1.communityhub.domain.NotificationEvent;
import com.m1.communityhub.security.AuthenticatedUser;
import com.m1.communityhub.security.SecurityUtils;
import com.m1.communityhub.service.NotificationService;
import com.m1.communityhub.service.NotificationSseService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseController {
    private final NotificationSseService sseService;
    private final NotificationService notificationService;

    public SseController(NotificationSseService sseService, NotificationService notificationService) {
        this.sseService = sseService;
        this.notificationService = notificationService;
    }

    @GetMapping("/sse/notifications")
    public SseEmitter streamNotifications(@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        AuthenticatedUser user = SecurityUtils.currentUser();
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        SseEmitter emitter = sseService.register(user.getId());
        if (lastEventId != null && !lastEventId.isBlank()) {
            try {
                Long lastId = Long.parseLong(lastEventId.trim());
                List<NotificationEvent> missed = notificationService.listEventsAfter(user.getId(), lastId, 50);
                missed.forEach(event -> sseService.sendNotification(user.getId(), notificationService.toDto(event, null)));
            } catch (NumberFormatException ex) {
                // ignore invalid last id
            }
        }
        return emitter;
    }
}
