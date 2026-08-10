package com.examen.reglementrag.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pipeline d'indexation RAG : PDF -> extraction texte -> decoupage en chunks
 * -> embeddings -> stockage dans pgvector (table vector_store, geree par Spring AI).
 *
 * Ne connait rien de JPA : prend juste un chemin de fichier + un id de document
 * (utilise comme cle de liaison en metadata).
 */
@Service
public class IndexationService {

    private static final String METADATA_DOCUMENT_ID = "documentId";

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    public IndexationService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.textSplitter = new TokenTextSplitter();
    }

    /**
     * Indexe un PDF deja stocke sur disque.
     * @return le nombre de chunks generes et ajoutes a pgvector.
     */
    public int indexDocument(Long documentId, String cheminStockage) {
        // 1. Extraction : un Document Spring AI par page du PDF (via Apache PDFBox)
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(new FileSystemResource(cheminStockage));
        List<Document> pages = pdfReader.get();

        // 2. Decoupage en chunks de taille raisonnable pour l'embedding (par defaut ~800 tokens)
        List<Document> chunks = textSplitter.apply(pages);

        // 3. On rattache chaque chunk a son document d'origine via la metadata
        List<Document> chunksAvecMetadata = chunks.stream()
                .map(chunk -> Document.builder()
                        .text(chunk.getText())
                        .metadata(enrichirMetadata(chunk.getMetadata(), documentId))
                        .build())
                .toList();

        // 4. Generation des embeddings (appel Ollama) + insertion dans pgvector
        //    C'est VectorStore.add() qui declenche tout ca automatiquement.
        vectorStore.add(chunksAvecMetadata);

        return chunksAvecMetadata.size();
    }

    /** Supprime tous les chunks/vecteurs associes a un document donne. */
    public void supprimerChunksDuDocument(Long documentId) {
        vectorStore.delete(METADATA_DOCUMENT_ID + " == '" + documentId + "'");
    }

    private Map<String, Object> enrichirMetadata(Map<String, Object> original, Long documentId) {
        Map<String, Object> metadata = new HashMap<>(original);
        metadata.put(METADATA_DOCUMENT_ID, String.valueOf(documentId));
        return metadata;
    }

}
