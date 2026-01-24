package com.m1.communityhub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m1.communityhub.dto.NotificationDtos;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationSseServiceTests {
    @Test
    void sendNotificationToRegisteredEmitter() throws Exception {
        NotificationSseService service = new NotificationSseService();
        SseEmitter emitter = mock(SseEmitter.class);
        doNothing().when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        service.registerEmitter(1L, emitter);

        NotificationDtos.NotificationResponse payload = new NotificationDtos.NotificationResponse(
            10L,
            "COMMENT_CREATED",
            2L,
            1L,
            "POST",
            99L,
            new ObjectMapper().createObjectNode().put("postId", 99),
            OffsetDateTime.now(),
            null
        );

        service.sendNotification(1L, payload);

        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
    }
}
