package com.m1.communityhub.repo;

import com.m1.communityhub.domain.GroupEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    Optional<GroupEntity> findBySlug(String slug);
}
