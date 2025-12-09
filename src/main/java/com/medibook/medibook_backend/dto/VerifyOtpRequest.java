package com.medibook.medibook_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VerifyOtpRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "OTP is required")
    private String otp;

    // Constructors
    public VerifyOtpRequest() {
    }

    public VerifyOtpRequest(Long userId, String role, String otp) {
        this.userId = userId;
        this.role = role;
        this.otp = otp;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
