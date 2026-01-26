package com.m1.communityhub.dto;

import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private String type;
    private Long actorId;
    private Long targetUserId;
    private String entityType;
    private Long entityId;
    private Map<String, Object> payload;
    private Instant createdAt;
    private Instant readAt;
}
