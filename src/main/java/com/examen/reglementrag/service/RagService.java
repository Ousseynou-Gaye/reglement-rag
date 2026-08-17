package com.examen.reglementrag.service;

import com.examen.reglementrag.dto.SourceReference;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Coeur du chatbot RAG : recherche par similarite dans pgvector, construction
 * d'un prompt qui force le LLM a repondre UNIQUEMENT a partir du contexte trouve,
 * puis appel a Ollama. Retourne aussi les sources (document + page) utilisees.
 */
@Service
public class RagService {

    /** Nombre de chunks les plus pertinents a recuperer pour construire le contexte. */
    private static final int TOP_K = 5;

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RagService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    public RagAnswer repondre(String question) {
        // 1. Recherche par similarite : on convertit la question en embedding
        //    et on retrouve les TOP_K chunks les plus proches semantiquement dans pgvector
        List<Document> chunksPertinents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(TOP_K)
                        .build()
        );

        String contexte = chunksPertinents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        // 2. Construction du prompt RAG : la consigne stricte est ce qui empeche
        //    le LLM d'halluciner ou de repondre avec des connaissances generales
        String prompt = """
                Tu es un assistant qui aide les etudiants a comprendre le reglement \
                pedagogique de leur etablissement.

                Reponds UNIQUEMENT a partir du contexte ci-dessous, extrait du reglement.
                Si l'information demandee ne s'y trouve pas, reponds explicitement que \
                cette information n'est pas presente dans le reglement fourni. \
                N'invente jamais de reponse et n'utilise aucune connaissance exterieure \
                au contexte.

                Contexte :
                %s

                Question : %s
                """.formatted(contexte, question);

        // 3. Appel au LLM (Ollama / llama3.1) avec ce prompt enrichi
        String reponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 4. On extrait les sources (documentId + page) depuis les metadata des chunks
        //    utilises, en dedupliquant (LinkedHashSet garde l'ordre de pertinence)
        List<SourceReference> sources = extraireSources(chunksPertinents);

        return new RagAnswer(reponse, sources);
    }

    private List<SourceReference> extraireSources(List<Document> chunks) {
        Set<SourceReference> vues = new LinkedHashSet<>();
        for (Document chunk : chunks) {
            var metadata = chunk.getMetadata();
            Long documentId = metadata.get("documentId") != null
                    ? Long.valueOf(String.valueOf(metadata.get("documentId")))
                    : null;
            Integer pageNumber = metadata.get("page_number") != null
                    ? Integer.valueOf(String.valueOf(metadata.get("page_number")))
                    : null;
            if (documentId != null) {
                vues.add(new SourceReference(documentId, pageNumber));
            }
        }
        return List.copyOf(vues);
    }

}
