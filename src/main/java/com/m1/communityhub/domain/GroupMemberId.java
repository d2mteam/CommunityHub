package com.m1.communityhub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class GroupMemberId implements Serializable {
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "user_id")
    private UUID userId;
}
