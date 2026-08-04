package com.examen.reglementrag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de sante utilise pour valider la connexion a Ollama (Seance 1).
 * A retirer ou proteger avant la mise en production.
 */
@RestController
@Tag(name = "Ping", description = "Verification de la connexion au LLM Ollama")
public class PingController {

    private final ChatClient chatClient;

    public PingController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Operation(summary = "Ping simple du modele Ollama configure (llama3.1)")
    @GetMapping("/api/ping")
    public String ping() {
        return chatClient.prompt()
                .user("Reponds uniquement par le mot OK, sans rien ajouter.")
                .call()
                .content();
    }

}
