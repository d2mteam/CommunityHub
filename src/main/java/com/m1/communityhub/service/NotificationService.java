package com.m1.communityhub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.m1.communityhub.domain.Comment;
import com.m1.communityhub.domain.NotificationEntityType;
import com.m1.communityhub.domain.NotificationEvent;
import com.m1.communityhub.domain.NotificationInbox;
import com.m1.communityhub.domain.NotificationType;
import com.m1.communityhub.domain.Post;
import com.m1.communityhub.domain.UserEntity;
import com.m1.communityhub.dto.NotificationDto;
import com.m1.communityhub.mapper.NotificationMapper;
import com.m1.communityhub.repo.NotificationEventRepository;
import com.m1.communityhub.repo.NotificationInboxRepository;
import com.m1.communityhub.web.ApiException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationEventRepository eventRepository;
    private final NotificationInboxRepository inboxRepository;
    private final ObjectMapper objectMapper;
    private final NotificationSseService sseService;
    private final NotificationMapper notificationMapper;

    public NotificationService(
        NotificationEventRepository eventRepository,
        NotificationInboxRepository inboxRepository,
        ObjectMapper objectMapper,
        NotificationSseService sseService,
        NotificationMapper notificationMapper
    ) {
        this.eventRepository = eventRepository;
        this.inboxRepository = inboxRepository;
        this.objectMapper = objectMapper;
        this.sseService = sseService;
        this.notificationMapper = notificationMapper;
    }

    @Transactional
    public void notifyCommentCreated(Comment comment, Post post, UserEntity actor) {
        UserEntity target = post.getAuthor();
        if (target.getId().equals(actor.getId())) {
            return;
        }
        NotificationEvent event = buildEvent(NotificationType.COMMENT_CREATED, actor, target, NotificationEntityType.POST,
            post.getId(), comment, null);
        NotificationEvent saved = eventRepository.save(event);
        inboxRepository.save(new NotificationInbox(target, saved));
        sseService.sendNotification(target.getId(), toDto(saved, null));
    }

    @Transactional
    public void notifyReplyCreated(Comment reply, Comment parent, Post post, UserEntity actor) {
        UserEntity target = parent.getAuthor();
        if (target.getId().equals(actor.getId())) {
            return;
        }
        NotificationEvent event = buildEvent(NotificationType.REPLY_CREATED, actor, target, NotificationEntityType.COMMENT,
            parent.getId(), reply, parent.getId());
        NotificationEvent saved = eventRepository.save(event);
        inboxRepository.save(new NotificationInbox(target, saved));
        sseService.sendNotification(target.getId(), toDto(saved, null));
    }

    public List<NotificationInbox> listInbox(Long userId, String cursor, int limit) {
        CursorParams params = CursorParams.from(cursor);
        return inboxRepository.findByUserWithCursor(userId, params.createdAt, params.id, PageRequest.of(0, limit));
    }

    @Transactional
    public void markRead(Long userId, Long eventId) {
        int updated = inboxRepository.markRead(userId, eventId, OffsetDateTime.now());
        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Notification not found");
        }
    }

    @Transactional
    public void markAllRead(Long userId) {
        inboxRepository.markAllRead(userId, OffsetDateTime.now());
    }

    public List<NotificationEvent> listEventsAfter(Long userId, Long lastEventId, int limit) {
        return eventRepository.findByUserAfterId(userId, lastEventId, PageRequest.of(0, limit));
    }

    public NotificationDto toDto(NotificationEvent event, OffsetDateTime readAt) {
        return notificationMapper.toDto(event, readAt);
    }

    private NotificationEvent buildEvent(
        NotificationType type,
        UserEntity actor,
        UserEntity target,
        NotificationEntityType entityType,
        Long entityId,
        Comment comment,
        Long parentCommentId
    ) {
        NotificationEvent event = new NotificationEvent();
        event.setType(type);
        event.setActor(actor);
        event.setTargetUser(target);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setPayload(buildPayload(comment, parentCommentId));
        return event;
    }

    private String buildPayload(Comment comment, Long parentCommentId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("postId", comment.getPost().getId());
        payload.put("commentId", comment.getId());
        payload.put("parentCommentId", parentCommentId);
        String preview = comment.getBody();
        if (preview != null && preview.length() > 120) {
            preview = preview.substring(0, 120);
        }
        payload.put("preview", preview);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to serialize notification payload");
        }
    }

    private record CursorParams(OffsetDateTime createdAt, Long id) {
        private static CursorParams from(String cursor) {
            if (cursor == null || cursor.isBlank()) {
                return new CursorParams(null, null);
            }
            try {
                String decoded = new String(java.util.Base64.getUrlDecoder().decode(cursor), java.nio.charset.StandardCharsets.UTF_8);
                String[] parts = decoded.split("\\|");
                return new CursorParams(OffsetDateTime.parse(parts[0]), Long.parseLong(parts[1]));
            } catch (Exception ex) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid cursor");
            }
        }
    }
}
