package com.m1.communityhub.service;

import com.m1.communityhub.dto.NotificationDto;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class NotificationSseService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationSseService.class);
    private static final long EMITTER_TIMEOUT_MS = Duration.ofMinutes(30).toMillis();

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter register(Long userId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        registerEmitter(userId, emitter);
        return emitter;
    }

    void registerEmitter(Long userId, SseEmitter emitter) {
        emitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(error -> removeEmitter(userId, emitter));
    }

    public void sendNotification(Long userId, NotificationDto payload) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .id(String.valueOf(payload.getId()))
                    .data(payload));
            } catch (IOException ex) {
                removeEmitter(userId, emitter);
            }
        }
    }

    @Scheduled(fixedRate = 20000)
    public void sendHeartbeat() {
        for (Map.Entry<Long, CopyOnWriteArrayList<SseEmitter>> entry : emitters.entrySet()) {
            Long userId = entry.getKey();
            for (SseEmitter emitter : entry.getValue()) {
                try {
                    emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
                } catch (IOException ex) {
                    logger.debug("SSE heartbeat failed", ex);
                    removeEmitter(userId, emitter);
                }
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        emitters.forEach((userId, list) -> list.forEach(this::completeEmitter));
        emitters.clear();
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
        }
        completeEmitter(emitter);
    }

    private void completeEmitter(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception ex) {
            logger.debug("SSE completion failed", ex);
        }
    }
}
