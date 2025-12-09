package com.medibook.medibook_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String adminEmail;

    public void sendApprovalEmail(String toEmail, String name, String tempPassword) {
        String subject = "MedVault Account Approved";
        String message = "Hello " + name + ",\n\n"
                + "Your MedVault account has been approved by the admin.\n"
                + "Here is your temporary password: " + tempPassword + "\n\n"
                + "Regards,\n"
                + "MedVault Admin";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(adminEmail); // always admin
        mailMessage.setTo(toEmail); // patient/doctor email
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }

    public void sendPasswordChangeConfirmationEmail(String toEmail) {
        String subject = "Password Reset";
        String message = "Hello " + toEmail + ",\n\n" +
                "Your password has been successfully reset.\n" +
                "If you did not perform this action, please contact our support immediately.\n\n" +
                "Best Regards,\n" +
                "MedVault Team";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(adminEmail);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }

    /**
     * Send OTP email for registration
     */
    public void sendOtpEmail(String toEmail, String name, String otpCode) {
        String subject = "MedVault - Your OTP Code";
        String message = "Hello " + name + ",\n\n" +
                "Your OTP code for MedVault registration is: " + otpCode + "\n\n" +
                "This OTP is valid for 10 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Best Regards,\n" +
                "MedVault Team";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(adminEmail);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }

    /**
     * Send approval email (new flow without temp password)
     */
    /**
     * Send approval email (new flow without temp password)
     */
    public void sendApprovalEmailNew(String toEmail, String name) {
        String subject = "MedVault Account Approved";
        String message = "Hello " + name + ",\n\n" +
                "Your MedVault account has been approved by the admin.\n" +
                "You can now log in using the password you created during registration/profile completion.\n\n" +
                "Best Regards,\n" +
                "MedVault Admin";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(adminEmail);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }

    /**
     * Send invitation email to new admin with temporary password
     */
    public void sendAdminInvitationEmail(String toEmail, String name, String tempPassword) {
        String subject = "MedVault Admin Invitation";
        String message = "Hello " + name + ",\n\n" +
                "You have been invited to join MedVault as an Administrator.\n" +
                "Please log in with the following temporary credentials and complete your profile:\n\n" +
                "Email: " + toEmail + "\n" +
                "Temporary Password: " + tempPassword + "\n\n" +
                "Upon first login, you will be required to change your password and complete your profile details.\n\n"
                +
                "Best Regards,\n" +
                "MedVault Team";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(adminEmail);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }
}
