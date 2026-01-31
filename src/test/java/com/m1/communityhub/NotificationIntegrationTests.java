package com.m1.communityhub;

import com.m1.communityhub.domain.Comment;
import com.m1.communityhub.domain.GroupEntity;
import com.m1.communityhub.domain.GroupMember;
import com.m1.communityhub.domain.Post;
import com.m1.communityhub.domain.UserEntity;
import com.m1.communityhub.dto.CommentDtos;
import com.m1.communityhub.repo.GroupMemberRepository;
import com.m1.communityhub.repo.GroupRepository;
import com.m1.communityhub.repo.NotificationEventRepository;
import com.m1.communityhub.repo.NotificationInboxRepository;
import com.m1.communityhub.repo.PostRepository;
import com.m1.communityhub.repo.UserRepository;
import com.m1.communityhub.security.UserContext;
import com.m1.communityhub.service.CommentService;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
class NotificationIntegrationTests {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("communityhub")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> "http://localhost:9999/does-not-exist");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private NotificationEventRepository eventRepository;

    @Autowired
    private NotificationInboxRepository inboxRepository;

    @Test
    void commentCreatesNotificationForPostAuthor() {
        UserEntity author = userRepository.save(user("author", "author@example.com"));
        UserEntity commenter = userRepository.save(user("commenter", "commenter@example.com"));
        GroupEntity group = groupRepository.save(group("group-one", "Group One", author));
        groupMemberRepository.save(new GroupMember(group, author));
        groupMemberRepository.save(new GroupMember(group, commenter));
        Post post = postRepository.save(post(group, author));

        commentService.createComment(
            post.getId(),
            userContext(commenter.getId(), commenter.getUsername()),
            new CommentDtos.CommentCreateRequest("hello", null)
        );

        assertThat(eventRepository.findAll()).hasSize(1);
        assertThat(inboxRepository.findAll()).hasSize(1);
        assertThat(eventRepository.findAll().getFirst().getTargetUser().getId()).isEqualTo(author.getId());
    }

    @Test
    void replyCreatesNotificationForParentAuthor() {
        UserEntity author = userRepository.save(user("author2", "author2@example.com"));
        UserEntity replier = userRepository.save(user("replier", "replier@example.com"));
        GroupEntity group = groupRepository.save(group("group-two", "Group Two", author));
        groupMemberRepository.save(new GroupMember(group, author));
        groupMemberRepository.save(new GroupMember(group, replier));
        Post post = postRepository.save(post(group, author));
        Comment parent = commentService.createComment(
            post.getId(),
            userContext(author.getId(), author.getUsername()),
            new CommentDtos.CommentCreateRequest("parent", null)
        );

        commentService.createComment(
            post.getId(),
            userContext(replier.getId(), replier.getUsername()),
            new CommentDtos.CommentCreateRequest("reply", parent.getId())
        );

        assertThat(eventRepository.findAll()).hasSize(1);
        assertThat(inboxRepository.findAll()).hasSize(1);
        assertThat(eventRepository.findAll().getFirst().getTargetUser().getId()).isEqualTo(author.getId());
    }

    private UserEntity user(String username, String email) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }

    private GroupEntity group(String slug, String name, UserEntity owner) {
        GroupEntity group = new GroupEntity();
        group.setSlug(slug);
        group.setName(name);
        group.setOwner(owner);
        return group;
    }

    private UserContext userContext(Long id, String username) {
        return new UserContext(String.valueOf(id), username, username + "@example.com", Set.of(), Set.of());
    }

    private Post post(GroupEntity group, UserEntity author) {
        Post post = new Post();
        post.setGroup(group);
        post.setAuthor(author);
        post.setTitle("Hello");
        post.setBody("Body");
        return post;
    }
}
