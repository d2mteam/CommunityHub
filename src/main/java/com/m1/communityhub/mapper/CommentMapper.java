package com.m1.communityhub.mapper;

import com.m1.communityhub.domain.Comment;
import com.m1.communityhub.dto.CommentCreateRequest;
import com.m1.communityhub.dto.CommentDto;
import com.m1.communityhub.dto.CommentUpdateRequest;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toEntity(CommentCreateRequest request);

    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "createdAt", source = "createdAt")
    CommentDto toDto(Comment comment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget Comment comment, CommentUpdateRequest request);

    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }
}
