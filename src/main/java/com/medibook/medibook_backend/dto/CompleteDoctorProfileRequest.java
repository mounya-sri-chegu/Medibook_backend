package com.medibook.medibook_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CompleteDoctorProfileRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    private String gender;

    private String profilePhotoPath; // Set after file upload

    @NotBlank(message = "Medical registration number is required")
    private String medicalRegistrationNumber;

    @NotBlank(message = "Licensing authority is required")
    private String licensingAuthority;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @NotBlank(message = "Qualification is required")
    private String qualification;

    @NotNull(message = "Experience is required")
    private Integer experience;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Clinic/Hospital name is required")
    private String clinicHospitalName;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    private String medicalLicensePath; // Set after file upload

    private String degreeCertificatesPath; // Set after file upload

    // Constructors
    public CompleteDoctorProfileRequest() {
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfilePhotoPath() {
        return profilePhotoPath;
    }

    public void setProfilePhotoPath(String profilePhotoPath) {
        this.profilePhotoPath = profilePhotoPath;
    }

    public String getMedicalRegistrationNumber() {
        return medicalRegistrationNumber;
    }

    public void setMedicalRegistrationNumber(String medicalRegistrationNumber) {
        this.medicalRegistrationNumber = medicalRegistrationNumber;
    }

    public String getLicensingAuthority() {
        return licensingAuthority;
    }

    public void setLicensingAuthority(String licensingAuthority) {
        this.licensingAuthority = licensingAuthority;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getClinicHospitalName() {
        return clinicHospitalName;
    }

    public void setClinicHospitalName(String clinicHospitalName) {
        this.clinicHospitalName = clinicHospitalName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getMedicalLicensePath() {
        return medicalLicensePath;
    }

    public void setMedicalLicensePath(String medicalLicensePath) {
        this.medicalLicensePath = medicalLicensePath;
    }

    public String getDegreeCertificatesPath() {
        return degreeCertificatesPath;
    }

    public void setDegreeCertificatesPath(String degreeCertificatesPath) {
        this.degreeCertificatesPath = degreeCertificatesPath;
    }
}
