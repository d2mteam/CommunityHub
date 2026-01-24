package com.m1.communityhub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notif_inbox")
public class NotificationInbox {
    @EmbeddedId
    private NotificationInboxId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private NotificationEvent event;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    public NotificationInbox() {
    }

    public NotificationInbox(UserEntity user, NotificationEvent event) {
        this.user = user;
        this.event = event;
        this.id = new NotificationInboxId(user.getId(), event.getId());
    }

    public NotificationInboxId getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public NotificationEvent getEvent() {
        return event;
    }

    public OffsetDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(OffsetDateTime readAt) {
        this.readAt = readAt;
    }
}
