package com.m1.communityhub.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m1.communityhub.domain.NotificationEntityType;
import com.m1.communityhub.domain.NotificationEvent;
import com.m1.communityhub.domain.NotificationType;
import com.m1.communityhub.domain.UserEntity;
import com.m1.communityhub.dto.NotificationDto;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMapperTests {
    @Test
    void mapsNotificationEventToDto() {
        NotificationMapperImpl mapper = new NotificationMapperImpl();
        mapper.setObjectMapper(new ObjectMapper());

        UserEntity actor = new UserEntity();
        actor.setId(1L);
        UserEntity target = new UserEntity();
        target.setId(2L);
        NotificationEvent event = new NotificationEvent();
        event.setId(99L);
        event.setType(NotificationType.COMMENT_CREATED);
        event.setActor(actor);
        event.setTargetUser(target);
        event.setEntityType(NotificationEntityType.POST);
        event.setEntityId(10L);
        event.setPayload("{\"postId\":10}");
        event.setCreatedAt(OffsetDateTime.now());

        NotificationDto dto = mapper.toDto(event, OffsetDateTime.now());

        assertThat(dto.getId()).isEqualTo(99L);
        assertThat(dto.getActorId()).isEqualTo(1L);
        assertThat(dto.getTargetUserId()).isEqualTo(2L);
        assertThat(dto.getEntityId()).isEqualTo(10L);
        assertThat(dto.getPayload()).containsEntry("postId", 10);
        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getReadAt()).isNotNull();
    }
}
