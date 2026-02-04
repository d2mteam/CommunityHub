package com.m1.communityhub.config.security.permission;

import com.m1.communityhub.config.security.pro.SecurityUtils;
import com.m1.communityhub.config.security.pro.UserContext;
import com.m1.communityhub.domain.Comment;
import com.m1.communityhub.domain.Post;
import com.m1.communityhub.domain.enums.CommentStatus;
import com.m1.communityhub.domain.enums.PostStatus;
import com.m1.communityhub.repo.CommentRepository;
import com.m1.communityhub.repo.PostRepository;
import com.m1.communityhub.service.GroupService;
import com.m1.communityhub.web.ApiException;
import java.lang.reflect.Method;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class GroupPermissionEvaluator {
    private final GroupService groupService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(permission)")
    public Object evaluate(ProceedingJoinPoint joinPoint, HasGroupPermission permission) throws Throwable {
        UserContext user = resolveUser(joinPoint.getArgs());
        UUID userId = requireUserId(user);
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Long targetId = resolveTargetId(method, joinPoint.getArgs(), permission.targetIdParam());
        enforce(permission.resource(), permission.action(), targetId, userId);
        return joinPoint.proceed();
    }

    private void enforce(PermissionResource resource, PermissionAction action, Long targetId, UUID userId) {
        switch (resource) {
            case GROUP -> ensureGroupPermission(action, targetId, userId);
            case POST -> ensurePostPermission(action, targetId, userId);
            case COMMENT -> ensureCommentPermission(action, targetId, userId);
        }
    }

    private void ensureGroupPermission(PermissionAction action, Long groupId, UUID userId) {
        if (action != PermissionAction.CREATE) {
            groupService.ensureActiveMember(groupId, userId);
        }
    }

    private void ensurePostPermission(PermissionAction action, Long targetId, UUID userId) {
        if (action == PermissionAction.CREATE) {
            groupService.ensureActiveMember(targetId, userId);
            return;
        }
        Post post = postRepository.findByIdAndStatusNot(targetId, PostStatus.DELETED)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found"));
        groupService.ensureActiveMember(post.getGroup().getId(), userId);
        if (!post.getAuthor().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not the post author");
        }
    }

    private void ensureCommentPermission(PermissionAction action, Long targetId, UUID userId) {
        if (action == PermissionAction.CREATE) {
            Post post = postRepository.findByIdAndStatusNot(targetId, PostStatus.DELETED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found"));
            groupService.ensureActiveMember(post.getGroup().getId(), userId);
            return;
        }
        Comment comment = commentRepository.findByIdAndStatusNot(targetId, CommentStatus.DELETED)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comment not found"));
        groupService.ensureActiveMember(comment.getPost().getGroup().getId(), userId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not the comment author");
        }
    }

    private Long resolveTargetId(Method method, Object[] args, String paramName) {
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        if (parameterNames == null) {
            throw new IllegalStateException("Missing parameter names for " + method.getName());
        }
        for (int index = 0; index < parameterNames.length; index++) {
            if (paramName.equals(parameterNames[index])) {
                return coerceToLong(args[index], paramName);
            }
        }
        throw new IllegalStateException("Missing parameter " + paramName + " for " + method.getName());
    }

    private Long coerceToLong(Object value, String paramName) {
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (value instanceof String stringValue) {
            return Long.valueOf(stringValue);
        }
        throw new IllegalStateException("Unsupported parameter type for " + paramName);
    }

    private UserContext resolveUser(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof UserContext user) {
                return user;
            }
        }
        UserContext user = SecurityUtils.currentUser();
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return user;
    }

    private UUID requireUserId(UserContext userContext) {
        try {
            return UUID.fromString(userContext.userId());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid user id");
        }
    }
}
