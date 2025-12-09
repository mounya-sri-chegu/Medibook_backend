package com.medibook.medibook_backend.service;

import com.medibook.medibook_backend.config.FileStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final FileStorageProperties fileStorageProperties;

    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
        initializeDirectories();
    }

    private void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(fileStorageProperties.getBaseDir()));
            Files.createDirectories(Paths.get(fileStorageProperties.getAdminCertDir()));
            Files.createDirectories(Paths.get(fileStorageProperties.getDoctorCertDir()));
            Files.createDirectories(Paths.get(fileStorageProperties.getPatientIdProofDir()));
            log.info("File storage directories initialized successfully");
        } catch (IOException e) {
            log.error("Could not initialize file storage directories", e);
            throw new RuntimeException("Could not initialize folder for upload!", e);
        }
    }

    public String saveAdminCertificate(MultipartFile file) {
        return saveFile(file, fileStorageProperties.getAdminCertDir(), "admin-certificates");
    }

    public String saveDoctorCertificate(MultipartFile file) {
        return saveFile(file, fileStorageProperties.getDoctorCertDir(), "doctor-certificates");
    }

    public String savePatientIdProof(MultipartFile file) {
        return saveFile(file, fileStorageProperties.getPatientIdProofDir(), "patient-id-proofs");
    }

    private String saveFile(MultipartFile file, String directory, String urlPath) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Cannot store empty file");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.contains("..")) {
                throw new RuntimeException("Invalid filename: " + originalFilename);
            }

            String filename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path targetPath = Paths.get(directory).resolve(filename);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved successfully: {}", filename);

            return "/files/" + urlPath + "/" + filename;
        } catch (IOException e) {
            log.error("Could not store file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage(), e);
        }
    }

    // Legacy method for backward compatibility
    @Deprecated
    public String save(MultipartFile file) {
        return saveFile(file, fileStorageProperties.getBaseDir(), "uploads");
    }
}
