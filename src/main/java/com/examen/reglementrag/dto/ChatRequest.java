package com.examen.reglementrag.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "La question ne peut pas etre vide")
        String question
) {}
