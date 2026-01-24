package com.m1.communityhub.repo;

import com.m1.communityhub.domain.GroupMember;
import com.m1.communityhub.domain.GroupMemberId;
import com.m1.communityhub.domain.GroupMemberState;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {
    Optional<GroupMember> findByIdGroupIdAndIdUserId(Long groupId, Long userId);

    boolean existsByIdGroupIdAndIdUserIdAndState(Long groupId, Long userId, GroupMemberState state);
}
