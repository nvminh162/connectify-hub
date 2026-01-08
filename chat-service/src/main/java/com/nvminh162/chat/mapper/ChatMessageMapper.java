package com.nvminh162.chat.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.nvminh162.chat.dto.request.ChatMessageRequest;
import com.nvminh162.chat.dto.response.ChatMessageResponse;
import com.nvminh162.chat.entity.ChatMessage;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);

    ChatMessage toChatMessage(ChatMessageRequest request);

    List<ChatMessageResponse> toChatMessageResponses(List<ChatMessage> chatMessages);
}
