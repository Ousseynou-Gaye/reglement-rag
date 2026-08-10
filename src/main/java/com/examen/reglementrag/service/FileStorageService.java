package com.examen.reglementrag.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Responsable UNIQUEMENT du stockage physique des fichiers PDF sur disque.
 * Ne connait rien de JPA, ni du RAG : simple entree/sortie fichier.
 */
@Service
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService(@Value("${app.storage.upload-dir}") String uploadDirProperty) {
        this.uploadDir = Paths.get(uploadDirProperty).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de creer le dossier de stockage : " + this.uploadDir, e);
        }
    }

    /**
     * Enregistre le fichier sur disque et retourne son chemin absolu.
     * Rejette tout ce qui n'est pas un PDF.
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier envoye est vide.");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf"
        );

        if (!originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Seuls les fichiers PDF sont acceptes (recu : " + originalFilename + ")");
        }

        // Prefixe timestamp pour eviter les collisions si deux fichiers portent le meme nom
        String storedFilename = System.currentTimeMillis() + "_" + originalFilename;
        Path targetPath = uploadDir.resolve(storedFilename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Erreur lors de l'enregistrement du fichier sur disque.", e);
        }

        return targetPath.toString();
    }

    /** Supprime le fichier physique (utilise lors du DELETE /api/documents/{id}). */
    public void delete(String cheminStockage) {
        try {
            Files.deleteIfExists(Paths.get(cheminStockage));
        } catch (IOException e) {
            // On log sans bloquer la suppression en base : le fichier orphelin
            // sera un probleme mineur, pas une raison de faire echouer l'API.
            System.err.println("Impossible de supprimer le fichier physique : " + cheminStockage);
        }
    }

}
