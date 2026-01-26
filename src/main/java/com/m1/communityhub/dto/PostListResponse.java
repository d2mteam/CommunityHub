package com.m1.communityhub.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {
    private List<PostDto> items;
    private String nextCursor;
}
