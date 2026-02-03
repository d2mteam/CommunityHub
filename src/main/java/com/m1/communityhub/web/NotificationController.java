package com.m1.communityhub.web;

import com.m1.communityhub.domain.NotificationInbox;
import com.m1.communityhub.dto.NotificationDtos;
import com.m1.communityhub.config.security.SecurityUtils;
import com.m1.communityhub.config.security.UserContext;
import com.m1.communityhub.service.NotificationService;
import com.m1.communityhub.util.CursorUtils;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public NotificationDtos.NotificationListResponse listNotifications(
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "20") int limit
    ) {
        UserContext user = requireUser();
        UUID userId = requireUserId(user);
        List<NotificationInbox> inboxItems = notificationService.listInbox(userId, cursor, limit);
        String nextCursor = inboxItems.isEmpty()
            ? null
            : CursorUtils.encode(inboxItems.getLast().getEvent().getCreatedAt(), inboxItems.getLast().getEvent().getId());
        List<NotificationDtos.NotificationResponse> items = inboxItems.stream()
            .map(inbox -> notificationService.toDto(inbox.getEvent(), inbox.getReadAt()))
            .toList();
        return new NotificationDtos.NotificationListResponse(items, nextCursor);
    }

    @PostMapping("/{eventId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable Long eventId) {
        UserContext user = requireUser();
        UUID userId = requireUserId(user);
        notificationService.markRead(userId, eventId);
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead() {
        UserContext user = requireUser();
        UUID userId = requireUserId(user);
        notificationService.markAllRead(userId);
    }

    private UserContext requireUser() {
        UserContext user = SecurityUtils.currentUser();
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return user;
    }

    private UUID requireUserId(UserContext user) {
        try {
            return UUID.fromString(user.userId());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid user id");
        }
    }
}
