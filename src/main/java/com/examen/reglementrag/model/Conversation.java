package com.examen.reglementrag.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Historique d'une question posee par un etudiant et de la reponse generee par le RAG.
 * Utilise en Seance 3 (endpoint POST /api/chat) et pour le bonus "historique des conversations".
 */
@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String reponse;

    @Column(nullable = false)
    private LocalDateTime dateEchange;

}
