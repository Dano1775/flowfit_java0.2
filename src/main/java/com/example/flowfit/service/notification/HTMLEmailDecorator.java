package com.example.flowfit.service.notification;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
  Decorador concreto que agrega formato HTML al email
  Este decorador envuelve una notificación de email y le agrega
  capacidad de enviar contenido HTML en lugar de texto plano. 
  Patrón GoF: DECORATOR (Decorador Concreto)
  Responsabilidad: Agregar formato HTML al email
 **/
public class HTMLEmailDecorator extends EmailNotificationDecorator {
    
    private final JavaMailSender mailSender;
    
    public HTMLEmailDecorator(EmailNotification notification, JavaMailSender mailSender) {
        super(notification);
        this.mailSender = mailSender;
    }
    
    @Override
    public String getContentType() {
        return "text/html; charset=UTF-8";
    }
    
    @Override
    public String getContent() {
        String baseContent = super.getContent();
        
        // Si el contenido ya tiene tags HTML, devolverlo tal cual
        if (baseContent.trim().startsWith("<")) {
            return baseContent;
        }
        
        // Envolver contenido simple en HTML
        return "<html><body style='font-family: Arial, sans-serif;'>" +
               "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
               baseContent +
               "</div></body></html>";
    }
    
    @Override
    public boolean send() {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(getRecipient());
            helper.setSubject(getSubject());
            helper.setText(getContent(), true); // true = HTML
            
            mailSender.send(message);
            System.out.println("Email HTML enviado a: " + getRecipient());
            return true;
        } catch (Exception e) {
            System.err.println("Error al enviar email HTML: " + e.getMessage());
            return false;
        }
    }
}
