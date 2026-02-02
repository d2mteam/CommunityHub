package com.m1.communityhub.repo;

import com.m1.communityhub.domain.NotificationEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {
    @Query("""
        select e from NotificationEvent e
        where e.targetUser.id = :userId
          and (:cursorCreatedAt is null
            or (e.createdAt < :cursorCreatedAt
              or (e.createdAt = :cursorCreatedAt and e.id < :cursorId)))
        order by e.createdAt desc, e.id desc
    """)
    List<NotificationEvent> findByUserWithCursor(
        @Param("userId") UUID userId,
        @Param("cursorCreatedAt") java.time.OffsetDateTime cursorCreatedAt,
        @Param("cursorId") Long cursorId,
        Pageable pageable
    );

    @Query("""
        select e from NotificationEvent e
        where e.targetUser.id = :userId
          and e.id > :afterId
        order by e.id asc
    """)
    List<NotificationEvent> findByUserAfterId(
        @Param("userId") UUID userId,
        @Param("afterId") Long afterId,
        Pageable pageable
    );
}
