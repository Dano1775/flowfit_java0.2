package com.example.flowfit.service.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Factory para crear notificaciones de email con decoradores
 * 
 * Esta clase proporciona métodos convenientes para crear notificaciones
 * de email ya decoradas según las necesidades del sistema.
 * 
 * Patrón GoF: DECORATOR + FACTORY
 * Propósito: Simplificar la creación de emails decorados
 */
@Component
public class EmailNotificationFactory {
    
    @Autowired
    private JavaMailSender mailSender;
    
    /**
     * Crea un email simple de texto plano
     */
    public EmailNotification createSimpleEmail(String recipient, String subject, String content) {
        return new SimpleEmailNotification(recipient, subject, content, mailSender);
    }
    
    /**
     * Crea un email con formato HTML
     */
    public EmailNotification createHTMLEmail(String recipient, String subject, String htmlContent) {
        EmailNotification base = new SimpleEmailNotification(recipient, subject, htmlContent, mailSender);
        return new HTMLEmailDecorator(base, mailSender);
    }
    
    /**
     * Crea un email HTML con prioridad alta
     */
    public EmailNotification createUrgentHTMLEmail(String recipient, String subject, String htmlContent) {
        EmailNotification base = new SimpleEmailNotification(recipient, subject, htmlContent, mailSender);
        EmailNotification withHTML = new HTMLEmailDecorator(base, mailSender);
        return new PriorityEmailDecorator(withHTML, 1, mailSender); // Prioridad 1 = ALTA
    }
    
    /**
     * Crea un email simple con prioridad alta
     */
    public EmailNotification createUrgentEmail(String recipient, String subject, String content) {
        EmailNotification base = new SimpleEmailNotification(recipient, subject, content, mailSender);
        return new PriorityEmailDecorator(base, 1, mailSender);
    }
    
    /**
     * Crea un email HTML con prioridad personalizada
     */
    public EmailNotification createCustomPriorityEmail(String recipient, String subject, String htmlContent, int priority) {
        EmailNotification base = new SimpleEmailNotification(recipient, subject, htmlContent, mailSender);
        EmailNotification withHTML = new HTMLEmailDecorator(base, mailSender);
        return new PriorityEmailDecorator(withHTML, priority, mailSender);
    }
}
