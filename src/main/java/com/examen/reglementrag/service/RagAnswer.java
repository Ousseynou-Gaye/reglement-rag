package com.examen.reglementrag.service;

import com.examen.reglementrag.dto.SourceReference;

import java.util.List;

/** Resultat interne du RagService : la reponse generee + les passages source utilises. */
public record RagAnswer(String reponse, List<SourceReference> sources) {}
