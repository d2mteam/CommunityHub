package com.m1.communityhub.config.security.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HasGroupPermission {
    PermissionResource resource();
    PermissionAction action();
    String targetIdParam();
}
