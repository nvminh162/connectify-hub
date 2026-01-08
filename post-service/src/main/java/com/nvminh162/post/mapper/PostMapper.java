package com.nvminh162.post.mapper;

import com.nvminh162.post.dto.response.PostResponse;
import com.nvminh162.post.entity.Post;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostResponse toPostResponse(Post post);
}
