package com.m1.communityhub.mapper;

import com.m1.communityhub.domain.Post;
import com.m1.communityhub.dto.PostCreateRequest;
import com.m1.communityhub.dto.PostDto;
import com.m1.communityhub.dto.PostUpdateRequest;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PostMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Post toEntity(PostCreateRequest request);

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "createdAt", source = "createdAt")
    PostDto toDto(Post post);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget Post post, PostUpdateRequest request);

    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }
}
