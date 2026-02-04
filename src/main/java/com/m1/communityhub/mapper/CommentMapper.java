package com.m1.communityhub.mapper;

import com.m1.communityhub.domain.Comment;
import com.m1.communityhub.dto.CommentDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "status", target = "status")
    CommentDtos.CommentResponse toResponse(Comment comment);
}
