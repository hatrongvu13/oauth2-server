package com.htv.oauth2.service.email;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Template;
import io.quarkus.qute.Location;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    @Inject
    @Location("emails/mfa-setup")
    Template mfaSetupTemplate;

    @Inject
    @Location("emails/login-notification")
    Template loginNotificationTemplate;

    @Inject
    @Location("emails/password-reset")
    Template passwordResetTemplate;

    /**
     * Send MFA setup email with QR code
     */
    public void sendMfaSetupEmail(String to, String username, String qrCodeBase64, String secretKey) {
        try {
            String html = mfaSetupTemplate
                    .data("username", username)
                    .data("qrCode", qrCodeBase64)
                    .data("secretKey", secretKey)
                    .render();

            mailer.send(Mail.withHtml(to, "Setup Two-Factor Authentication", html));
            log.info("MFA setup email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send MFA setup email to: {}", to, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    /**
     * Send login notification
     */
    public void sendLoginNotification(String to, String username, String ipAddress, String userAgent) {
        try {
            String html = loginNotificationTemplate
                    .data("username", username)
                    .data("ipAddress", ipAddress)
                    .data("userAgent", userAgent)
                    .data("timestamp", java.time.LocalDateTime.now())
                    .render();

            mailer.send(Mail.withHtml(to, "New Login Detected", html));
            log.info("Login notification sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send login notification to: {}", to, e);
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String to, String username, String resetToken) {
        try {
            String resetUrl = "http://localhost:8080/reset-password?token=" + resetToken;

            String html = passwordResetTemplate
                    .data("username", username)
                    .data("resetUrl", resetUrl)
                    .render();

            mailer.send(Mail.withHtml(to, "Password Reset Request", html));
            log.info("Password reset email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    /**
     * Send MFA backup codes
     */
    public void sendBackupCodes(String to, String username, String backupCodes) {
        try {
            String html = String.format("""
                <html>
                <body>
                    <h2>Your MFA Backup Codes</h2>
                    <p>Hi %s,</p>
                    <p>Here are your backup codes for two-factor authentication. Keep them safe!</p>
                    <pre>%s</pre>
                    <p><strong>Important:</strong> Each code can only be used once.</p>
                </body>
                </html>
                """, username, backupCodes.replace(",", "\n"));

            mailer.send(Mail.withHtml(to, "Your MFA Backup Codes", html));
            log.info("Backup codes email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send backup codes email to: {}", to, e);
        }
    }
}