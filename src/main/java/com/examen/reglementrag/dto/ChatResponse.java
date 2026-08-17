package com.examen.reglementrag.dto;

import com.examen.reglementrag.model.Conversation;

import java.time.LocalDateTime;
import java.util.List;

public record ChatResponse(
        String question,
        String reponse,
        List<SourceReference> sources,
        LocalDateTime dateEchange
) {
    public static ChatResponse fromEntity(Conversation conversation) {
        return new ChatResponse(
                conversation.getQuestion(),
                conversation.getReponse(),
                List.of(), // l'historique ne re-stocke pas les sources (voir Conversation)
                conversation.getDateEchange()
        );
    }
}
