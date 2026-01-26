package com.m1.communityhub.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.m1.communityhub.domain.NotificationEvent;
import com.m1.communityhub.dto.NotificationDto;
import com.m1.communityhub.web.ApiException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
@Setter
public abstract class NotificationMapper {
    @Autowired
    private ObjectMapper objectMapper;

    @Mapping(target = "actorId", source = "actor.id")
    @Mapping(target = "targetUserId", source = "targetUser.id")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "entityType", source = "entityType")
    @Mapping(target = "payload", source = "payload")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "readAt", source = "readAt")
    public abstract NotificationDto toDto(NotificationEvent event, OffsetDateTime readAt);

    public Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    public Map<String, Object> map(String payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to parse notification payload");
        }
    }
}
