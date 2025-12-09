package com.medibook.medibook_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "medvault.upload")
public class FileStorageProperties {

    private String baseDir = "uploads";
    private String adminCertDir = "uploads/admin-certificates";
    private String doctorCertDir = "uploads/doctor-certificates";
    private String patientIdProofDir = "uploads/patient-id-proofs";

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getAdminCertDir() {
        return adminCertDir;
    }

    public void setAdminCertDir(String adminCertDir) {
        this.adminCertDir = adminCertDir;
    }

    public String getDoctorCertDir() {
        return doctorCertDir;
    }

    public void setDoctorCertDir(String doctorCertDir) {
        this.doctorCertDir = doctorCertDir;
    }

    public String getPatientIdProofDir() {
        return patientIdProofDir;
    }

    public void setPatientIdProofDir(String patientIdProofDir) {
        this.patientIdProofDir = patientIdProofDir;
    }
}
