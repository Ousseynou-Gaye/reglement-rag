package com.examen.reglementrag.exception;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(Long id) {
        super("Aucun document trouve avec l'id " + id);
    }
}
