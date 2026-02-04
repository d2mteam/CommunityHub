package com.m1.communityhub.mapper;

import com.m1.communityhub.domain.Post;
import com.m1.communityhub.dto.PostDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMapper {
    @Mapping(source = "group.id", target = "groupId")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "status", target = "status")
    PostDtos.PostResponse toResponse(Post post);
}
