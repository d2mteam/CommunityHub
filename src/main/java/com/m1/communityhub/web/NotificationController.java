package com.m1.communityhub.web;

import com.m1.communityhub.domain.NotificationInbox;
import com.m1.communityhub.dto.NotificationDto;
import com.m1.communityhub.dto.NotificationListResponse;
import com.m1.communityhub.security.AuthenticatedUser;
import com.m1.communityhub.security.SecurityUtils;
import com.m1.communityhub.service.NotificationService;
import com.m1.communityhub.util.CursorUtils;
import java.util.List;
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
    public NotificationListResponse listNotifications(
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "20") int limit
    ) {
        AuthenticatedUser user = requireUser();
        List<NotificationInbox> inboxItems = notificationService.listInbox(user.getId(), cursor, limit);
        String nextCursor = inboxItems.isEmpty()
            ? null
            : CursorUtils.encode(inboxItems.getLast().getEvent().getCreatedAt(), inboxItems.getLast().getEvent().getId());
        List<NotificationDto> items = inboxItems.stream()
            .map(inbox -> notificationService.toDto(inbox.getEvent(), inbox.getReadAt()))
            .toList();
        return new NotificationListResponse(items, nextCursor);
    }

    @PostMapping("/{eventId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable Long eventId) {
        AuthenticatedUser user = requireUser();
        notificationService.markRead(user.getId(), eventId);
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead() {
        AuthenticatedUser user = requireUser();
        notificationService.markAllRead(user.getId());
    }

    private AuthenticatedUser requireUser() {
        AuthenticatedUser user = SecurityUtils.currentUser();
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return user;
    }
}
