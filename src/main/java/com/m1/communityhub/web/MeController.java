package com.m1.communityhub.web;

import com.m1.communityhub.domain.UserEntity;
import com.m1.communityhub.dto.AuthDtos;
import com.m1.communityhub.repo.UserRepository;
import com.m1.communityhub.security.AuthenticatedUser;
import com.m1.communityhub.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {
    private final UserRepository userRepository;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public AuthDtos.MeResponse me() {
        AuthenticatedUser current = SecurityUtils.currentUser();
        if (current == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        UserEntity user = userRepository.findById(current.getId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return new AuthDtos.MeResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}
