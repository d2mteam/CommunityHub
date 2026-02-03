package com.m1.communityhub.web;

import com.m1.communityhub.dto.UserDtos;
import com.m1.communityhub.config.security.SecurityUtils;
import com.m1.communityhub.config.security.UserContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {
    @GetMapping("/me")
    public UserDtos.MeResponse me() {
        UserContext current = SecurityUtils.currentUser();
        if (current == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return new UserDtos.MeResponse(
            current.userId(),
            current.username(),
            current.email(),
            current.roles(),
            current.scopes()
        );
    }
}
