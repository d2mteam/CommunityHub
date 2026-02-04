package com.m1.communityhub.mapper;

import com.m1.communityhub.domain.GroupEntity;
import com.m1.communityhub.dto.GroupDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    @Mapping(source = "owner.id", target = "ownerId")
    GroupDtos.GroupResponse toResponse(GroupEntity group);
}
