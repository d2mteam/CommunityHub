package com.m1.communityhub.mapper;

import com.m1.communityhub.domain.Comment;
import com.m1.communityhub.domain.CommentStatus;
import com.m1.communityhub.domain.Post;
import com.m1.communityhub.domain.UserEntity;
import com.m1.communityhub.dto.CommentCreateRequest;
import com.m1.communityhub.dto.CommentDto;
import com.m1.communityhub.dto.CommentUpdateRequest;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTests {
    private final CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

    @Test
    void createRequestDoesNotSetServerFields() {
        CommentCreateRequest request = new CommentCreateRequest("Body", 12L);
        Comment comment = commentMapper.toEntity(request);

        assertThat(comment.getId()).isNull();
        assertThat(comment.getPost()).isNull();
        assertThat(comment.getAuthor()).isNull();
        assertThat(comment.getParent()).isNull();
        assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
    }

    @Test
    void updateRequestIgnoresNulls() {
        Comment comment = new Comment();
        comment.setBody("Original");

        CommentUpdateRequest request = new CommentUpdateRequest(null);
        commentMapper.update(comment, request);

        assertThat(comment.getBody()).isEqualTo("Original");
    }

    @Test
    void mapsEntityToDtoWithIds() {
        UserEntity author = new UserEntity();
        author.setId(10L);
        Post post = new Post();
        post.setId(20L);
        Comment parent = new Comment();
        parent.setId(30L);
        Comment comment = new Comment();
        comment.setId(40L);
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setParent(parent);
        comment.setBody("Body");
        comment.setStatus(CommentStatus.ACTIVE);
        comment.setCreatedAt(OffsetDateTime.now());

        CommentDto dto = commentMapper.toDto(comment);

        assertThat(dto.getAuthorId()).isEqualTo(10L);
        assertThat(dto.getPostId()).isEqualTo(20L);
        assertThat(dto.getParentId()).isEqualTo(30L);
        assertThat(dto.getCreatedAt()).isNotNull();
    }
}
