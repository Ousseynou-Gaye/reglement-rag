package com.examen.reglementrag.controller;

import com.examen.reglementrag.dto.ChatRequest;
import com.examen.reglementrag.dto.ChatResponse;
import com.examen.reglementrag.model.Conversation;
import com.examen.reglementrag.repository.ConversationRepository;
import com.examen.reglementrag.service.RagAnswer;
import com.examen.reglementrag.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Question-reponse RAG sur le reglement pedagogique indexe")
public class ChatController {

    private final RagService ragService;
    private final ConversationRepository conversationRepository;

    public ChatController(RagService ragService, ConversationRepository conversationRepository) {
        this.ragService = ragService;
        this.conversationRepository = conversationRepository;
    }

    @Operation(summary = "Poser une question : recherche par similarite dans pgvector, puis reponse generee par le LLM, avec citation des sources")
    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        RagAnswer ragAnswer = ragService.repondre(request.question());

        Conversation conversation = Conversation.builder()
                .question(request.question())
                .reponse(ragAnswer.reponse())
                .dateEchange(LocalDateTime.now())
                .build();
        conversation = conversationRepository.save(conversation);

        return new ChatResponse(
                conversation.getQuestion(),
                conversation.getReponse(),
                ragAnswer.sources(),
                conversation.getDateEchange()
        );
    }

    @Operation(summary = "Historique des questions/reponses precedentes (bonus)")
    @GetMapping("/history")
    public List<ChatResponse> history() {
        return conversationRepository.findAll().stream()
                .map(ChatResponse::fromEntity)
                .toList();
    }

}
