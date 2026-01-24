package com.m1.communityhub.repo;

import com.m1.communityhub.domain.Comment;
import com.m1.communityhub.domain.CommentStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
        select c from Comment c
        where c.post.id = :postId
          and c.status <> :deletedStatus
          and (:cursorCreatedAt is null
            or (c.createdAt > :cursorCreatedAt
              or (c.createdAt = :cursorCreatedAt and c.id > :cursorId)))
        order by c.createdAt asc, c.id asc
        """)
    List<Comment> findByPostWithCursor(
        @Param("postId") Long postId,
        @Param("deletedStatus") CommentStatus deletedStatus,
        @Param("cursorCreatedAt") OffsetDateTime cursorCreatedAt,
        @Param("cursorId") Long cursorId,
        Pageable pageable
    );

    Optional<Comment> findByIdAndStatusNot(Long id, CommentStatus status);
}
