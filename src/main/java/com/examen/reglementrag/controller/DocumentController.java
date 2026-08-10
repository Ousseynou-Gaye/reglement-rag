package com.examen.reglementrag.controller;

import com.examen.reglementrag.dto.DocumentResponse;
import com.examen.reglementrag.exception.DocumentNotFoundException;
import com.examen.reglementrag.model.DocumentEntity;
import com.examen.reglementrag.model.DocumentStatus;
import com.examen.reglementrag.repository.DocumentRepository;
import com.examen.reglementrag.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Import, consultation et suppression des documents PDF")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    public DocumentController(DocumentRepository documentRepository, FileStorageService fileStorageService) {
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "Importer un PDF (etape 1 : stockage + enregistrement en base, statut EN_ATTENTE)")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(@RequestParam("file") MultipartFile file) {
        String cheminStockage = fileStorageService.store(file);

        DocumentEntity document = DocumentEntity.builder()
                .nomFichier(file.getOriginalFilename())
                .cheminStockage(cheminStockage)
                .statut(DocumentStatus.EN_ATTENTE)
                .dateImport(LocalDateTime.now())
                .build();

        document = documentRepository.save(document);

        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentResponse.fromEntity(document));
    }

    @Operation(summary = "Lister tous les documents indexes")
    @GetMapping
    public List<DocumentResponse> list() {
        return documentRepository.findAll().stream()
                .map(DocumentResponse::fromEntity)
                .toList();
    }

    @Operation(summary = "Supprimer un document et son fichier physique (les vecteurs seront geres en Seance 2 - partie indexation)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        fileStorageService.delete(document.getCheminStockage());
        documentRepository.delete(document);

        return ResponseEntity.noContent().build();
    }

}
