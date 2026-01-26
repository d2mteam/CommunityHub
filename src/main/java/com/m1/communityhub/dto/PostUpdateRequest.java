package com.m1.communityhub.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {
    @Size(max = 255)
    private String title;

    private String body;
}
