package com.examen.reglementrag.dto;

import com.examen.reglementrag.model.Conversation;

import java.time.LocalDateTime;

public record ChatResponse(
        String question,
        String reponse,
        LocalDateTime dateEchange
) {
    public static ChatResponse fromEntity(Conversation conversation) {
        return new ChatResponse(
                conversation.getQuestion(),
                conversation.getReponse(),
                conversation.getDateEchange()
        );
    }
}
