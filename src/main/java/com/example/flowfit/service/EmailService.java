package com.example.flowfit.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.stereotype.Service;
import java.util.Properties;

@Service
public class EmailService {

    private static final String REMITENTE = "0flowfit0@gmail.com";
    private static final String PASSWORD = "pbvg igyq ticm xqgq";

    /**
     * Envía correo de bienvenida al usuario registrado
     */
    public boolean enviarCorreoBienvenida(String destinatario, String nombreUsuario, String tipoUsuario) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("¡Bienvenido a FlowFit!");

            String contenido = construirMensajeBienvenida(nombreUsuario, tipoUsuario);
            message.setContent(contenido, "text/html; charset=utf-8");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Construye el mensaje HTML de bienvenida
     */
    private String construirMensajeBienvenida(String nombreUsuario, String tipoUsuario) {
        if ("Entrenador".equals(tipoUsuario) || "Nutricionista".equals(tipoUsuario)) {
            return String.format(
                    "<html><body style='font-family: Arial, sans-serif;'>" +
                            "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>"
                            +
                            "<div style='background-color: white; padding: 30px; border-radius: 10px;'>" +
                            "<h2 style='color: #4CAF50;'>¡Hola %s!</h2>" +
                            "<p>Tu solicitud de registro como <strong>%s</strong> ha sido recibida exitosamente.</p>" +
                            "<p>Un administrador revisará tu solicitud y te notificaremos cuando tu cuenta sea aprobada.</p>"
                            +
                            "<p>Gracias por tu interés en formar parte del equipo de FlowFit.</p>" +
                            "<hr style='border: 1px solid #eee; margin: 20px 0;'>" +
                            "<p style='color: #666;'>Saludos,<br><strong style='color: #4CAF50;'>Equipo FlowFit</strong></p>"
                            +
                            "</div>" +
                            "</div>" +
                            "</body></html>",
                    nombreUsuario, tipoUsuario);
        } else {
            return String.format(
                    "<html><body style='font-family: Arial, sans-serif;'>" +
                            "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>"
                            +
                            "<div style='background-color: white; padding: 30px; border-radius: 10px;'>" +
                            "<h2 style='color: #4CAF50;'>¡Bienvenido a FlowFit, %s!</h2>" +
                            "<p>Tu registro ha sido exitoso. Ya puedes <strong>iniciar sesión</strong> y disfrutar de todos nuestros servicios.</p>"
                            +
                            "<p>Estamos emocionados de acompañarte en tu viaje fitness.</p>" +
                            "<ul style='color: #555;'>" +
                            "<li>Accede a rutinas personalizadas</li>" +
                            "<li>Consulta tu historial de entrenamientos</li>" +
                            "<li>Mantente en contacto con tu entrenador</li>" +
                            "</ul>" +
                            "<hr style='border: 1px solid #eee; margin: 20px 0;'>" +
                            "<p style='color: #666;'>Saludos,<br><strong style='color: #4CAF50;'>Equipo FlowFit</strong></p>"
                            +
                            "</div>" +
                            "</div>" +
                            "</body></html>",
                    nombreUsuario);
        }
    }

    /**
     * Envía correo de aprobación de cuenta
     */
    public boolean enviarCorreoAprobacion(String destinatario, String nombreUsuario, String tipoUsuario) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("¡Tu cuenta ha sido aprobada en FlowFit!");

            String contenido = String.format(
                    "<html><body style='font-family: Arial, sans-serif;'>" +
                            "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>"
                            +
                            "<div style='background-color: white; padding: 30px; border-radius: 10px;'>" +
                            "<h2 style='color: #4CAF50;'>¡Felicidades %s!</h2>" +
                            "<p>Tu cuenta como <strong>%s</strong> ha sido <strong style='color: #4CAF50;'>aprobada</strong>.</p>"
                            +
                            "<p>Ya puedes iniciar sesión y comenzar a utilizar todas las funcionalidades de la plataforma.</p>"
                            +
                            "<p>¡Te damos la bienvenida al equipo FlowFit!</p>" +
                            "<hr style='border: 1px solid #eee; margin: 20px 0;'>" +
                            "<p style='color: #666;'>Saludos,<br><strong style='color: #4CAF50;'>Equipo FlowFit</strong></p>"
                            +
                            "</div>" +
                            "</div>" +
                            "</body></html>",
                    nombreUsuario, tipoUsuario);
            message.setContent(contenido, "text/html; charset=utf-8");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envía correo de rechazo de cuenta
     */
    public boolean enviarCorreoRechazo(String destinatario, String nombreUsuario, String tipoUsuario, String motivo) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("Actualización de tu solicitud en FlowFit");

            String motivoTexto = (motivo != null && !motivo.isEmpty())
                    ? "<p><strong>Motivo:</strong> " + motivo + "</p>"
                    : "";

            String contenido = String.format(
                    "<html><body style='font-family: Arial, sans-serif;'>" +
                            "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>"
                            +
                            "<div style='background-color: white; padding: 30px; border-radius: 10px;'>" +
                            "<h2 style='color: #FF5722;'>Hola %s</h2>" +
                            "<p>Lamentamos informarte que tu solicitud de registro como <strong>%s</strong> no ha sido aprobada en este momento.</p>"
                            +
                            "%s" +
                            "<p>Si tienes alguna pregunta o deseas más información, no dudes en contactarnos.</p>" +
                            "<hr style='border: 1px solid #eee; margin: 20px 0;'>" +
                            "<p style='color: #666;'>Saludos,<br><strong style='color: #4CAF50;'>Equipo FlowFit</strong></p>"
                            +
                            "</div>" +
                            "</div>" +
                            "</body></html>",
                    nombreUsuario, tipoUsuario, motivoTexto);
            message.setContent(contenido, "text/html; charset=utf-8");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
