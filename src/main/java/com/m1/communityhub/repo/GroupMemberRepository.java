package com.m1.communityhub.repo;

import com.m1.communityhub.domain.GroupMember;
import com.m1.communityhub.domain.GroupMemberId;
import com.m1.communityhub.domain.enums.GroupMemberState;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {
    Optional<GroupMember> findByIdGroupIdAndIdUserId(Long groupId, UUID userId);

    boolean existsByIdGroupIdAndIdUserIdAndState(Long groupId, UUID userId, GroupMemberState state);
}
