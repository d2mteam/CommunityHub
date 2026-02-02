package com.m1.communityhub.service;

import com.m1.communityhub.domain.GroupEntity;
import com.m1.communityhub.domain.GroupMember;
import com.m1.communityhub.domain.GroupMemberId;
import com.m1.communityhub.domain.enums.GroupMemberRole;
import com.m1.communityhub.domain.enums.GroupMemberState;
import com.m1.communityhub.domain.UserEntity;
import com.m1.communityhub.dto.GroupDtos;
import com.m1.communityhub.repo.GroupMemberRepository;
import com.m1.communityhub.repo.GroupRepository;
import com.m1.communityhub.repo.UserRepository;
import com.m1.communityhub.security.UserContext;
import com.m1.communityhub.web.ApiException;
import java.util.UUID;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupEntity createGroup(UserContext userContext, GroupDtos.GroupCreateRequest request) {
        UUID ownerId = requireUserId(userContext);
        groupRepository.findBySlug(request.getSlug()).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, "Group slug already exists");
        });
        UserEntity owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        GroupEntity group = new GroupEntity();
        group.setSlug(request.getSlug());
        group.setName(request.getName());
        group.setOwner(owner);
        group = groupRepository.save(group);

        GroupMember member = new GroupMember(group, owner);
        member.setRole(GroupMemberRole.OWNER);
        groupMemberRepository.save(member);
        return group;
    }

    public List<GroupEntity> listGroups() {
        return groupRepository.findAll();
    }

    public GroupEntity getGroup(Long groupId) {
        return groupRepository.findById(groupId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Group not found"));
    }

    @Transactional
    public void joinGroup(Long groupId, UserContext userContext) {
        UUID userId = requireUserId(userContext);
        GroupEntity group = getGroup(groupId);
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        GroupMember member = groupMemberRepository.findById(new GroupMemberId(group.getId(), user.getId()))
            .orElse(null);
        if (member == null) {
            groupMemberRepository.save(new GroupMember(group, user));
            return;
        }
        member.setState(GroupMemberState.ACTIVE);
    }

    @Transactional
    public void leaveGroup(Long groupId, UserContext userContext) {
        UUID userId = requireUserId(userContext);
        GroupMember member = groupMemberRepository.findById(new GroupMemberId(groupId, userId))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Membership not found"));
        member.setState(GroupMemberState.LEFT);
    }

    public void ensureActiveMember(Long groupId, UUID userId) {
        if (!groupMemberRepository.existsByIdGroupIdAndIdUserIdAndState(groupId, userId, GroupMemberState.ACTIVE)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User is not an active group member");
        }
    }

    private UUID requireUserId(UserContext userContext) {
        try {
            return UUID.fromString(userContext.userId());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid user id");
        }
    }
}
