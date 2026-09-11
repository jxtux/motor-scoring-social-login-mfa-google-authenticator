package com.finanscore.motorscoring.infrastructure.notification;

import com.finanscore.motorscoring.application.port.out.EmailSender;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class GmailSmtpEmailSender implements EmailSender {
    private final JavaMailSender mailSender;
    private final String remitente;

    public GmailSmtpEmailSender(JavaMailSender mailSender,
                                @Value("${spring.mail.username:}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public void enviarConAdjunto(String destinatario, String asunto, String contenido,
                                 String nombreAdjunto, byte[] adjunto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            if (remitente != null && !remitente.isBlank()) helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenido, false);
            helper.addAttachment(nombreAdjunto, new ByteArrayResource(adjunto), "application/pdf");
            mailSender.send(message);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo enviar el informe por SMTP", e);
        }
    }
}
