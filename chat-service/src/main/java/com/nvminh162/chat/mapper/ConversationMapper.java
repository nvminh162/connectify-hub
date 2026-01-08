package com.nvminh162.chat.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.nvminh162.chat.dto.response.ConversationResponse;
import com.nvminh162.chat.entity.Conversation;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    ConversationResponse toConversationResponse(Conversation conversation);

    List<ConversationResponse> toConversationResponseList(List<Conversation> conversations);
}
