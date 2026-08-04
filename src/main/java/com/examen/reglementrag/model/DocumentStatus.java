package com.examen.reglementrag.model;

/**
 * Statut du cycle de vie d'un document dans le pipeline d'indexation.
 */
public enum DocumentStatus {
    EN_ATTENTE,   // fichier recu, pas encore traite
    EN_COURS,     // decoupage / embeddings en cours
    INDEXE,       // pret a etre interroge
    ECHEC         // erreur lors de l'indexation
}
