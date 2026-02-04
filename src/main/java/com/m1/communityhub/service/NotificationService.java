package com.m1.communityhub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.m1.communityhub.domain.Comment;
import com.m1.communityhub.domain.enums.NotificationEntityType;
import com.m1.communityhub.domain.NotificationEvent;
import com.m1.communityhub.domain.NotificationInbox;
import com.m1.communityhub.domain.enums.NotificationType;
import com.m1.communityhub.domain.Post;
import com.m1.communityhub.domain.UserEntity;
import com.m1.communityhub.dto.NotificationDtos;
import com.m1.communityhub.repo.NotificationEventRepository;
import com.m1.communityhub.repo.NotificationInboxRepository;
import com.m1.communityhub.web.ApiException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationEventRepository eventRepository;
    private final NotificationInboxRepository inboxRepository;
    private final ObjectMapper objectMapper;
    private final NotificationSseService sseService;

    @Transactional
    public void notifyCommentCreated(Comment comment, Post post, UserEntity actor) {
        UserEntity target = post.getAuthor();
        if (target.getId().equals(actor.getId())) {
            return;
        }
        NotificationEvent event = buildEvent(NotificationType.COMMENT_CREATED, actor, target, NotificationEntityType.POST,
            post.getId(), comment, null);
        NotificationEvent saved = eventRepository.save(event);
        inboxRepository.save(NotificationInbox.builder()
            .id(new com.m1.communityhub.domain.NotificationInboxId(target.getId(), saved.getId()))
            .user(target)
            .event(saved)
            .build());
        sseService.sendNotification(target.getId(), toDto(saved, null));
    }

    @Transactional
    public void notifyReplyCreated(Comment reply, Comment parent, UserEntity actor) {
        UserEntity target = parent.getAuthor();
        if (target.getId().equals(actor.getId())) {
            return;
        }
        NotificationEvent event = buildEvent(NotificationType.REPLY_CREATED, actor, target, NotificationEntityType.COMMENT,
            parent.getId(), reply, parent.getId());
        NotificationEvent saved = eventRepository.save(event);
        inboxRepository.save(NotificationInbox.builder()
            .id(new com.m1.communityhub.domain.NotificationInboxId(target.getId(), saved.getId()))
            .user(target)
            .event(saved)
            .build());
        sseService.sendNotification(target.getId(), toDto(saved, null));
    }

    public List<NotificationInbox> listInbox(UUID userId, String cursor, int limit) {
        CursorParams params = CursorParams.from(cursor);
        return inboxRepository.findByUserWithCursor(userId, params.createdAt, params.id, PageRequest.of(0, limit));
    }

    @Transactional
    public void markRead(UUID userId, Long eventId) {
        int updated = inboxRepository.markRead(userId, eventId, OffsetDateTime.now());
        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Notification not found");
        }
    }

    @Transactional
    public void markAllRead(UUID userId) {
        inboxRepository.markAllRead(userId, OffsetDateTime.now());
    }

    public List<NotificationEvent> listEventsAfter(UUID userId, Long lastEventId, int limit) {
        return eventRepository.findByUserAfterId(userId, lastEventId, PageRequest.of(0, limit));
    }

    public NotificationDtos.NotificationResponse toDto(NotificationEvent event, OffsetDateTime readAt) {
        JsonNode payloadNode = null;
        if (event.getPayload() != null) {
            try {
                payloadNode = objectMapper.readTree(event.getPayload());
            } catch (JsonProcessingException ex) {
                payloadNode = objectMapper.getNodeFactory().textNode(event.getPayload());
            }
        }
        return new NotificationDtos.NotificationResponse(
            event.getId(),
            event.getType().name(),
            event.getActor() == null ? null : event.getActor().getId().toString(),
            event.getTargetUser() == null ? null : event.getTargetUser().getId().toString(),
            event.getEntityType().name(),
            event.getEntityId(),
            payloadNode,
            event.getCreatedAt(),
            readAt
        );
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
        return NotificationEvent.builder()
            .type(type)
            .actor(actor)
            .targetUser(target)
            .entityType(entityType)
            .entityId(entityId)
            .payload(buildPayload(comment, parentCommentId))
            .build();
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
