package com.m1.communityhub.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {
    private Long id;
    private Long groupId;
    private Long authorId;
    private String title;
    private String body;
    private String status;
    private Instant createdAt;
}
