package com.m1.communityhub.web;

import com.m1.communityhub.domain.GroupEntity;
import com.m1.communityhub.dto.GroupDtos;
import com.m1.communityhub.security.SecurityUtils;
import com.m1.communityhub.security.UserContext;
import com.m1.communityhub.service.GroupService;
import jakarta.validation.Valid;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupDtos.GroupResponse createGroup(@Valid @RequestBody GroupDtos.GroupCreateRequest request) {
        UserContext user = requireUser();
        GroupEntity group = groupService.createGroup(user, request);
        return toResponse(group);
    }

    @GetMapping
    public List<GroupDtos.GroupResponse> listGroups() {
        return groupService.listGroups().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{groupId}")
    public GroupDtos.GroupResponse getGroup(@PathVariable Long groupId) {
        return toResponse(groupService.getGroup(groupId));
    }

    @PostMapping("/{groupId}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void joinGroup(@PathVariable Long groupId) {
        UserContext user = requireUser();
        groupService.joinGroup(groupId, user);
    }

    @PostMapping("/{groupId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGroup(@PathVariable Long groupId) {
        UserContext user = requireUser();
        groupService.leaveGroup(groupId, user);
    }

    private UserContext requireUser() {
        UserContext user = SecurityUtils.currentUser();
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return user;
    }

    private GroupDtos.GroupResponse toResponse(GroupEntity group) {
        return new GroupDtos.GroupResponse(
            group.getId(),
            group.getSlug(),
            group.getName(),
            group.getOwner() == null ? null : group.getOwner().getId(),
            group.getCreatedAt()
        );
    }
}
