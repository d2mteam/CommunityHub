package com.m1.communityhub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "group_members")
public class GroupMember {
    @EmbeddedId
    private GroupMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupMemberRole role = GroupMemberRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupMemberState state = GroupMemberState.ACTIVE;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    public GroupMember() {
    }

    public GroupMember(GroupEntity group, UserEntity user) {
        this.group = group;
        this.user = user;
        this.id = new GroupMemberId(group.getId(), user.getId());
    }

    public GroupMemberId getId() {
        return id;
    }

    public GroupEntity getGroup() {
        return group;
    }

    public UserEntity getUser() {
        return user;
    }

    public GroupMemberRole getRole() {
        return role;
    }

    public void setRole(GroupMemberRole role) {
        this.role = role;
    }

    public GroupMemberState getState() {
        return state;
    }

    public void setState(GroupMemberState state) {
        this.state = state;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }
}
