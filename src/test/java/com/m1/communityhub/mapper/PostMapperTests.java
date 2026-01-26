package com.m1.communityhub.mapper;

import com.m1.communityhub.domain.GroupEntity;
import com.m1.communityhub.domain.Post;
import com.m1.communityhub.domain.PostStatus;
import com.m1.communityhub.domain.UserEntity;
import com.m1.communityhub.dto.PostCreateRequest;
import com.m1.communityhub.dto.PostDto;
import com.m1.communityhub.dto.PostUpdateRequest;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class PostMapperTests {
    private final PostMapper postMapper = Mappers.getMapper(PostMapper.class);

    @Test
    void createRequestDoesNotSetServerFields() {
        PostCreateRequest request = new PostCreateRequest("Title", "Body");
        Post post = postMapper.toEntity(request);

        assertThat(post.getId()).isNull();
        assertThat(post.getGroup()).isNull();
        assertThat(post.getAuthor()).isNull();
        assertThat(post.getStatus()).isEqualTo(PostStatus.ACTIVE);
    }

    @Test
    void updateRequestIgnoresNulls() {
        Post post = new Post();
        post.setTitle("Original");
        post.setBody("Body");

        PostUpdateRequest request = new PostUpdateRequest(null, "Updated");
        postMapper.update(post, request);

        assertThat(post.getTitle()).isEqualTo("Original");
        assertThat(post.getBody()).isEqualTo("Updated");
    }

    @Test
    void mapsEntityToDtoWithIds() {
        UserEntity author = new UserEntity();
        author.setId(10L);
        GroupEntity group = new GroupEntity();
        group.setId(20L);
        Post post = new Post();
        post.setId(30L);
        post.setAuthor(author);
        post.setGroup(group);
        post.setTitle("Title");
        post.setBody("Body");
        post.setStatus(PostStatus.ACTIVE);
        post.setCreatedAt(OffsetDateTime.now());

        PostDto dto = postMapper.toDto(post);

        assertThat(dto.getAuthorId()).isEqualTo(10L);
        assertThat(dto.getGroupId()).isEqualTo(20L);
        assertThat(dto.getId()).isEqualTo(30L);
        assertThat(dto.getCreatedAt()).isNotNull();
    }
}
