package com.examen.reglementrag.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represente un document PDF importe (ex: reglement pedagogique).
 * Table distincte de "vector_store" (geree automatiquement par Spring AI PgVectorStore)
 * qui stocke, elle, les chunks + embeddings.
 * Le lien entre les deux se fait via le champ "documentId" dans les metadata
 * de chaque Document Spring AI (voir IndexationService en Seance 2).
 */
@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomFichier;

    @Column(nullable = false)
    private String cheminStockage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus statut;

    @Column(nullable = false)
    private LocalDateTime dateImport;

    /** Nombre de chunks generes lors de l'indexation (rempli en Seance 2). */
    private Integer nombreChunks;

}
