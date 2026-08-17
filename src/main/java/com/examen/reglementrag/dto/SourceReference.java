package com.examen.reglementrag.dto;

/**
 * Reference vers un passage source ayant servi a construire la reponse RAG.
 * pageNumber peut etre null si l'information n'est pas disponible dans les metadata.
 */
public record SourceReference(
        Long documentId,
        Integer pageNumber
) {}
