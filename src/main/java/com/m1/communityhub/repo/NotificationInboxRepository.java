package com.m1.communityhub.repo;

import com.m1.communityhub.domain.NotificationInbox;
import com.m1.communityhub.domain.NotificationInboxId;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

public interface NotificationInboxRepository extends JpaRepository<NotificationInbox, NotificationInboxId> {
    @Query("""
        select n from NotificationInbox n
        join fetch n.event e
        where n.user.id = :userId
          and (:cursorCreatedAt is null
            or (e.createdAt < :cursorCreatedAt
              or (e.createdAt = :cursorCreatedAt and e.id < :cursorId)))
        order by e.createdAt desc, e.id desc
    """)
    List<NotificationInbox> findByUserWithCursor(
        @Param("userId") UUID userId,
        @Param("cursorCreatedAt") OffsetDateTime cursorCreatedAt,
        @Param("cursorId") Long cursorId,
        Pageable pageable
    );

    @Modifying
    @Query("update NotificationInbox n set n.readAt = :readAt where n.user.id = :userId and n.event.id = :eventId")
    int markRead(@Param("userId") UUID userId, @Param("eventId") Long eventId, @Param("readAt") OffsetDateTime readAt);

    @Modifying
    @Query("update NotificationInbox n set n.readAt = :readAt where n.user.id = :userId and n.readAt is null")
    int markAllRead(@Param("userId") UUID userId, @Param("readAt") OffsetDateTime readAt);
}
