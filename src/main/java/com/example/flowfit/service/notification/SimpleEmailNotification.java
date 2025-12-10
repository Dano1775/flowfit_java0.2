package com.example.flowfit.service.notification;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Implementación concreta base de EmailNotification
 * 
 * Esta clase representa una notificación de email simple sin decoradores.
 * Es la clase base que será "decorada" con funcionalidades adicionales.
 * 
 * Patrón GoF: DECORATOR (Componente Concreto)
 */
public class SimpleEmailNotification implements EmailNotification {
    
    private final String recipient;
    private final String subject;
    private final String content;
    private final JavaMailSender mailSender;
    
    public SimpleEmailNotification(String recipient, String subject, String content, JavaMailSender mailSender) {
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.mailSender = mailSender;
    }
    
    @Override
    public String getRecipient() {
        return recipient;
    }
    
    @Override
    public String getSubject() {
        return subject;
    }
    
    @Override
    public String getContent() {
        return content;
    }
    
    @Override
    public String getContentType() {
        return "text/plain";
    }
    
    @Override
    public int getPriority() {
        return 3; // Prioridad normal por defecto
    }
    
    @Override
    public boolean send() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            System.out.println("✅ Email simple enviado a: " + recipient);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error al enviar email: " + e.getMessage());
            return false;
        }
    }
}
