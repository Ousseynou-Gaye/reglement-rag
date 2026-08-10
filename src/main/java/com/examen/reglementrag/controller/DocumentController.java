package com.examen.reglementrag.controller;

import com.examen.reglementrag.dto.DocumentResponse;
import com.examen.reglementrag.exception.DocumentNotFoundException;
import com.examen.reglementrag.model.DocumentEntity;
import com.examen.reglementrag.model.DocumentStatus;
import com.examen.reglementrag.repository.DocumentRepository;
import com.examen.reglementrag.service.FileStorageService;
import com.examen.reglementrag.service.IndexationService;
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
    private final IndexationService indexationService;

    public DocumentController(DocumentRepository documentRepository,
                               FileStorageService fileStorageService,
                               IndexationService indexationService) {
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.indexationService = indexationService;
    }

    @Operation(summary = "Importer un PDF : stockage, extraction, decoupage en chunks, embeddings, indexation dans pgvector")
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

        try {
            document.setStatut(DocumentStatus.EN_COURS);
            documentRepository.save(document);

            int nombreChunks = indexationService.indexDocument(document.getId(), cheminStockage);

            document.setStatut(DocumentStatus.INDEXE);
            document.setNombreChunks(nombreChunks);
            document = documentRepository.save(document);
        } catch (Exception e) {
            document.setStatut(DocumentStatus.ECHEC);
            documentRepository.save(document);
            throw new IllegalStateException("Echec de l'indexation du document : " + e.getMessage(), e);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentResponse.fromEntity(document));
    }

    @Operation(summary = "Lister tous les documents indexes")
    @GetMapping
    public List<DocumentResponse> list() {
        return documentRepository.findAll().stream()
                .map(DocumentResponse::fromEntity)
                .toList();
    }

    @Operation(summary = "Supprimer un document : ses chunks/vecteurs dans pgvector, son fichier physique, puis son enregistrement en base")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        indexationService.supprimerChunksDuDocument(id);
        fileStorageService.delete(document.getCheminStockage());
        documentRepository.delete(document);

        return ResponseEntity.noContent().build();
    }

}
