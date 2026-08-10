package com.examen.reglementrag.dto;

import com.examen.reglementrag.model.DocumentEntity;
import com.examen.reglementrag.model.DocumentStatus;

import java.time.LocalDateTime;

/**
 * Vue publique d'un DocumentEntity, exposee par l'API.
 * On evite volontairement d'exposer "cheminStockage" (chemin serveur interne).
 */
public record DocumentResponse(
        Long id,
        String nomFichier,
        DocumentStatus statut,
        LocalDateTime dateImport,
        Integer nombreChunks
) {
    public static DocumentResponse fromEntity(DocumentEntity entity) {
        return new DocumentResponse(
                entity.getId(),
                entity.getNomFichier(),
                entity.getStatut(),
                entity.getDateImport(),
                entity.getNombreChunks()
        );
    }
}
