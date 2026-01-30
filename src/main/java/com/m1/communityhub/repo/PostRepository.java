package com.m1.communityhub.repo;

import com.m1.communityhub.domain.Post;
import com.m1.communityhub.domain.enums.PostStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("""
        select p from Post p
        where p.group.id = :groupId
          and p.status <> :deletedStatus
          and (:cursorCreatedAt is null
            or (p.createdAt < :cursorCreatedAt
              or (p.createdAt = :cursorCreatedAt and p.id < :cursorId)))
        order by p.createdAt desc, p.id desc
        """)
    List<Post> findByGroupWithCursor(
        @Param("groupId") Long groupId,
        @Param("deletedStatus") PostStatus deletedStatus,
        @Param("cursorCreatedAt") OffsetDateTime cursorCreatedAt,
        @Param("cursorId") Long cursorId,
        Pageable pageable
    );

    Optional<Post> findByIdAndStatusNot(Long id, PostStatus status);
}
