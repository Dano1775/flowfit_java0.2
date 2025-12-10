package com.example.flowfit.service.notification;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Decorador concreto que agrega prioridad alta al email
 * 
 * Este decorador envuelve una notificación de email y le agrega
 * headers de prioridad para que aparezca marcado como importante.
 * 
 * Patrón GoF: DECORATOR (Decorador Concreto)
 * Responsabilidad: Agregar prioridad alta al email (headers X-Priority)
 */
public class PriorityEmailDecorator extends EmailNotificationDecorator {
    
    private final int priorityLevel;
    private final JavaMailSender mailSender;
    
    /**
     * Constructor con nivel de prioridad personalizado
     * @param notification notificación base a decorar
     * @param priorityLevel nivel de prioridad (1=alta, 3=normal, 5=baja)
     * @param mailSender servicio de envío de emails
     */
    public PriorityEmailDecorator(EmailNotification notification, int priorityLevel, JavaMailSender mailSender) {
        super(notification);
        this.priorityLevel = priorityLevel;
        this.mailSender = mailSender;
    }
    
    /**
     * Constructor con prioridad alta por defecto
     */
    public PriorityEmailDecorator(EmailNotification notification, JavaMailSender mailSender) {
        this(notification, 1, mailSender); // 1 = Alta prioridad
    }
    
    @Override
    public int getPriority() {
        return priorityLevel;
    }
    
    @Override
    public String getSubject() {
        String baseSubject = super.getSubject();
        
        // Agregar indicador visual de prioridad
        if (priorityLevel == 1) {
            return "🔴 [URGENTE] " + baseSubject;
        } else if (priorityLevel == 2) {
            return "🟡 [IMPORTANTE] " + baseSubject;
        }
        
        return baseSubject;
    }
    
    @Override
    public boolean send() {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(getRecipient());
            helper.setSubject(getSubject());
            
            // Determinar si es HTML o texto plano
            boolean isHtml = getContentType().contains("html");
            helper.setText(getContent(), isHtml);
            
            // Agregar headers de prioridad
            message.setHeader("X-Priority", String.valueOf(priorityLevel));
            message.setHeader("Priority", getPriorityName());
            message.setHeader("Importance", getPriorityName());
            
            mailSender.send(message);
            System.out.println("✅ Email con prioridad " + getPriorityName() + " enviado a: " + getRecipient());
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error al enviar email con prioridad: " + e.getMessage());
            return false;
        }
    }
    
    private String getPriorityName() {
        return switch (priorityLevel) {
            case 1 -> "High";
            case 2 -> "Normal";
            case 5 -> "Low";
            default -> "Normal";
        };
    }
}
