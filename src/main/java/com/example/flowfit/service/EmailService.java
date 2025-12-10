package com.example.flowfit.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.File;
import java.util.Properties;

@Service
public class EmailService {

    private static final String REMITENTE = "0flowfit0@gmail.com";
    private static final String PASSWORD = "pbvg igyq ticm xqgq";
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;

    /**
     * Envía correo de bienvenida usando plantillas Thymeleaf
     * @param destinatario Email del destinatario
     * @param nombreUsuario Nombre del usuario registrado
     * @param correo Correo del usuario (para mostrar en el email)
     * @param tipoUsuario "ENTRENADOR" o "USUARIO"
     * @return true si el correo se envió exitosamente
     */
    public boolean enviarCorreoBienvenidaConPlantilla(String destinatario, String nombreUsuario, String correo, String tipoUsuario) {
        try {
            // Seleccionar template basado en tipo de usuario
            String templateName = tipoUsuario.equalsIgnoreCase("ENTRENADOR") 
                ? "email/welcome-entrenador" 
                : "email/welcome-usuario";
            
            // Crear contexto con variables para el template
            Context context = new Context();
            context.setVariable("nombre", nombreUsuario);
            context.setVariable("correo", correo);
            context.setVariable("urlDashboard", "http://localhost:8080/login");
            
            // Procesar el template
            String htmlContent = templateEngine.process(templateName, context);
            
            // Crear y enviar el mensaje
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(REMITENTE);
            helper.setTo(destinatario);
            helper.setSubject("¡Bienvenido a FlowFit! 💪");
            helper.setText(htmlContent, true); // true = HTML
            
            mailSender.send(message);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error al enviar correo de bienvenida: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envía correo de bienvenida al usuario registrado (Método original mantenido para compatibilidad)
     */
    public boolean enviarCorreoBienvenida(String destinatario, String nombreUsuario, String tipoUsuario) {
        try {
            // Configuración manual SMTP (mismo método que aprobación/rechazo)
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

            // Crear mensaje multipart para adjuntar logo
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("¡Bienvenido a FlowFit!");

            // Crear contenedor multipart
            MimeMultipart multipart = new MimeMultipart("related");

            // Parte HTML del correo
            MimeBodyPart htmlPart = new MimeBodyPart();
            String contenido = construirMensajeBienvenida(nombreUsuario, tipoUsuario);
            htmlPart.setContent(contenido, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // Adjuntar logo como inline
            try {
                MimeBodyPart imagePart = new MimeBodyPart();
                String logoPath = "src/main/resources/static/assets/logo_flowfit.png";
                File logoFile = new File(logoPath);
                
                if (logoFile.exists()) {
                    imagePart.attachFile(logoFile);
                    imagePart.setContentID("<flowfitLogo>");
                    imagePart.setDisposition(MimeBodyPart.INLINE);
                    multipart.addBodyPart(imagePart);
                } else {
                    System.out.println("⚠️ Logo no encontrado en: " + logoPath);
                }
            } catch (Exception imgEx) {
                System.err.println("Error al adjuntar logo: " + imgEx.getMessage());
                // Continuar sin logo si falla
            }

            message.setContent(multipart);
            
            Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Construye el mensaje HTML de bienvenida con el estilo FlowFit
     */
    private String construirMensajeBienvenida(String nombreUsuario, String tipoUsuario) {
        if ("Entrenador".equals(tipoUsuario) || "Nutricionista".equals(tipoUsuario)) {
            // Email para Entrenador/Nutricionista pendiente de aprobación - TEMA AZUL
            return String.format(
                    "<!DOCTYPE html>" +
                    "<html lang='es'>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "</head>" +
                    "<body style='margin: 0; padding: 0; font-family: \"Segoe UI\", Tahoma, sans-serif; background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%);'>" +
                    "    <table width='100%%' cellpadding='0' cellspacing='0' border='0' style='background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%); padding: 40px 20px;'>" +
                    "        <tr>" +
                    "            <td align='center'>" +
                    "                <table width='600' cellpadding='0' cellspacing='0' border='0' style='max-width: 600px; background: rgba(15, 23, 42, 0.85); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 16px; overflow: hidden; box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);'>" +
                    "                    " +
                    "                    <!-- Header con Logo FlowFit - Tema Azul Entrenador -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 50px 30px; text-align: center; background: rgba(59, 130, 246, 0.08); border-bottom: 1px solid rgba(59, 130, 246, 0.2);'>" +
                    "                            <!-- Logo FlowFit con círculo blanco de fondo -->" +
                    "                            <div style='margin-bottom: 20px;'>" +
                    "                                <div style='display: inline-block; width: 90px; height: 90px; background: #ffffff; border-radius: 50%%; padding: 15px; box-shadow: 0 4px 16px rgba(59, 130, 246, 0.25);'>" +
                    "                                    <img src='cid:flowfitLogo' alt='FlowFit Logo' style='width: 100%%; height: 100%%; object-fit: contain;' />" +
                    "                                </div>" +
                    "                            </div>" +
                    "                            <h1 style='margin: 10px 0 5px 0; color: #3b82f6; font-size: 32px; font-weight: 800; letter-spacing: -0.5px; text-shadow: 0 0 20px rgba(59, 130, 246, 0.3);'>FlowFit</h1>" +
                    "                            <p style='margin: 0; color: rgba(203, 213, 225, 0.8); font-size: 13px; font-weight: 500;'>Sistema de Gestión de Entrenamientos</p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    " +
                    "                    <!-- Contenido principal -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 40px 30px;'>" +
                    "                            <!-- Icono de estado pendiente -->" +
                    "                            <div style='text-align: center; margin-bottom: 30px;'>" +
                    "                                <span style='font-size: 72px; line-height: 1;'>⏳</span>" +
                    "                            </div>" +
                    "                            " +
                    "                            <h2 style='margin: 0 0 20px 0; color: #ffffff; font-size: 24px; font-weight: 700; text-align: center;'>¡Hola, %s!</h2>" +
                    "                            " +
                    "                            <p style='margin: 0 0 25px 0; color: #cbd5e1; font-size: 16px; line-height: 1.6; text-align: center;'>" +
                    "                                Tu solicitud de registro como <strong style='color: #3b82f6;'>%s</strong> ha sido recibida exitosamente." +
                    "                            </p>" +
                    "                            " +
                    "                            <!-- Tarjeta de información -->" +
                    "                            <div style='background: rgba(59, 130, 246, 0.08); border: 1px solid rgba(59, 130, 246, 0.15); border-left: 4px solid #3b82f6; border-radius: 12px; padding: 20px; margin: 25px 0;'>" +
                    "                                <p style='margin: 0 0 12px 0; color: #ffffff; font-size: 15px; font-weight: 600;'>📋 Próximos pasos:</p>" +
                    "                                <p style='margin: 0; color: #cbd5e1; font-size: 14px; line-height: 1.6;'>" +
                    "                                    Un administrador revisará tu solicitud. Te notificaremos por correo cuando tu cuenta sea aprobada. Este proceso suele tomar entre <strong>24-48 horas</strong>." +
                    "                                </p>" +
                    "                            </div>" +
                    "                            " +
                    "                            <!-- Botón Ver Estado -->" +
                    "                            <div style='text-align: center; margin: 35px 0 30px 0;'>" +
                    "                                <a href='http://localhost:8080/login' style='display: inline-block; background: linear-gradient(135deg, #fbbf24 0%%, #f59e0b 100%%); color: #0f172a; text-decoration: none; padding: 16px 40px; border-radius: 12px; font-weight: 700; font-size: 16px; box-shadow: 0 8px 24px rgba(251, 191, 36, 0.35); transition: all 0.3s ease;'>Ver Estado de Solicitud</a>" +
                    "                            </div>" +
                    "                            " +
                    "                            <p style='margin: 30px 0 0 0; color: rgba(203, 213, 225, 0.8); font-size: 14px; text-align: center; line-height: 1.6;'>" +
                    "                                Gracias por tu interés en formar parte del equipo de <strong style='color: #3b82f6;'>FlowFit</strong>. 💪" +
                    "                            </p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    " +
                    "                    <!-- Footer -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 30px; text-align: center; background: rgba(255, 255, 255, 0.02); border-top: 1px solid rgba(255, 255, 255, 0.08);'>" +
                    "                            <p style='margin: 0 0 10px 0; color: #cbd5e1; font-size: 14px;'>¿Tienes preguntas?</p>" +
                    "                            <p style='margin: 0 0 20px 0;'>" +
                    "                                <a href='mailto:soporte@flowfit.com' style='color: #3b82f6; text-decoration: none; font-weight: 600;'>soporte@flowfit.com</a>" +
                    "                            </p>" +
                    "                            <p style='margin: 0; color: rgba(148, 163, 184, 0.8); font-size: 12px;'>" +
                    "                                © 2025 FlowFit. Todos los derechos reservados." +
                    "                            </p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                </table>" +
                    "            </td>" +
                    "        </tr>" +
                    "    </table>" +
                    "</body>" +
                    "</html>",
                    nombreUsuario, tipoUsuario);
        } else {
            // Email para Usuario/Cliente (aprobado automáticamente) - MEJORADO
            return String.format(
                    "<!DOCTYPE html>" +
                    "<html lang='es'>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "</head>" +
                    "<body style='margin: 0; padding: 0; font-family: \"Segoe UI\", Tahoma, sans-serif; background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%);'>" +
                    "    <table width='100%%' cellpadding='0' cellspacing='0' border='0' style='background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%); padding: 40px 20px;'>" +
                    "        <tr>" +
                    "            <td align='center'>" +
                    "                <table width='600' cellpadding='0' cellspacing='0' border='0' style='max-width: 600px; background: rgba(15, 23, 42, 0.85); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 16px; overflow: hidden; box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);'>" +
                    "                    " +
                    "                    <!-- Header Verde Vibrante con Logo -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 50px 30px; text-align: center; background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); border-bottom: 1px solid rgba(255, 255, 255, 0.1);'>" +
                    "                            <!-- Logo FlowFit con círculo blanco de fondo -->" +
                    "                            <div style='margin-bottom: 20px;'>" +
                    "                                <div style='display: inline-block; width: 90px; height: 90px; background: #ffffff; border-radius: 50%%; padding: 15px; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);'>" +
                    "                                    <img src='cid:flowfitLogo' alt='FlowFit Logo' style='width: 100%%; height: 100%%; object-fit: contain;' />" +
                    "                                </div>" +
                    "                            </div>" +
                    "                            <h1 style='margin: 0 0 8px 0; color: #ffffff; font-size: 38px; font-weight: 800; letter-spacing: -0.5px; text-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);'>FlowFit</h1>" +
                    "                            <p style='margin: 0; color: rgba(255, 255, 255, 0.95); font-size: 15px; font-weight: 600;'>Tu centro de entrenamiento personal</p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    " +
                    "                    <!-- Contenido principal -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 40px 30px;'>" +
                    "                            <!-- Icono de bienvenida -->" +
                    "                            <div style='text-align: center; margin-bottom: 30px;'>" +
                    "                                <span style='font-size: 72px; line-height: 1;'>🎉</span>" +
                    "                            </div>" +
                    "                            " +
                    "                            <h2 style='margin: 0 0 15px 0; color: #ffffff; font-size: 26px; font-weight: 700; text-align: center;'>¡Bienvenido, %s!</h2>" +
                    "                            " +
                    "                            <p style='margin: 0 0 25px 0; color: #cbd5e1; font-size: 16px; line-height: 1.7; text-align: center;'>" +
                    "                                Tu registro ha sido exitoso. 🎉 ¡Estamos emocionados de acompañarte en tu viaje fitness!" +
                    "                            </p>" +
                    "                            " +
                    "                            <!-- Características destacadas -->" +
                    "                            <div style='background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 12px; padding: 25px; margin: 25px 0;'>" +
                    "                                <p style='margin: 0 0 15px 0; color: #10b981; font-size: 15px; font-weight: 700; text-align: center;'>✨ ¿Qué puedes hacer ahora?</p>" +
                    "                                <div style='display: block;'>" +
                    "                                    <p style='margin: 8px 0; color: #cbd5e1; font-size: 14px; line-height: 1.6;'>💪 Accede a tu panel de usuario personalizado</p>" +
                    "                                    <p style='margin: 8px 0; color: #cbd5e1; font-size: 14px; line-height: 1.6;'>📊 Visualiza tu progreso y estadísticas</p>" +
                    "                                    <p style='margin: 8px 0; color: #cbd5e1; font-size: 14px; line-height: 1.6;'>🏋️ Consulta rutinas asignadas por tu entrenador</p>" +
                    "                                    <p style='margin: 8px 0; color: #cbd5e1; font-size: 14px; line-height: 1.6;'>🎯 Establece y alcanza tus objetivos</p>" +
                    "                                </div>" +
                    "                            </div>" +
                    "                            " +
                    "                            <!-- Botón CTA destacado -->" +
                    "                            <div style='text-align: center; margin: 35px 0;'>" +
                    "                                <a href='http://localhost:8080/login' style='display: inline-flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); color: #ffffff; text-decoration: none; padding: 18px 45px; border-radius: 12px; font-weight: 700; font-size: 17px; box-shadow: 0 8px 24px rgba(16, 185, 129, 0.4); transition: all 0.3s ease;'>" +
                    "                                    <svg width='22' height='22' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg' style='margin-right: 10px;'>" +
                    "                                        <path d='M15 3H19C20.1046 3 21 3.89543 21 5V19C21 20.1046 20.1046 21 19 21H15M10 17L15 12M15 12L10 7M15 12H3' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" +
                    "                                    </svg>" +
                    "                                    Acceder a mi Panel" +
                    "                                </a>" +
                    "                            </div>" +
                    "                            " +
                    "                            <!-- Tarjeta motivacional -->" +
                    "                            <div style='background: linear-gradient(135deg, #fbbf24 0%%, #f59e0b 100%%); border-radius: 14px; padding: 22px 25px; margin: 30px 0; box-shadow: 0 4px 12px rgba(251, 191, 36, 0.3);'>" +
                    "                                <div style='display: flex; align-items: flex-start;'>" +
                    "                                    <div style='background: rgba(255, 255, 255, 0.25); border-radius: 10px; padding: 8px; margin-right: 15px; flex-shrink: 0;'>" +
                    "                                        <svg width='28' height='28' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
                    "                                            <path d='M13 10V3L4 14H11L11 21L20 10H13Z' stroke='%%231a2332' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" +
                    "                                        </svg>" +
                    "                                    </div>" +
                    "                                    <div>" +
                    "                                        <p style='margin: 0 0 10px 0; color: #1a2332; font-size: 17px; font-weight: 800;'>💡 Consejo de bienvenida</p>" +
                    "                                        <p style='margin: 0; color: #1a2332; font-size: 15px; line-height: 1.6; font-weight: 500;'>" +
                    "                                            El éxito es la suma de pequeños esfuerzos repetidos día tras día. ¡Tu transformación comienza hoy!" +
                    "                                        </p>" +
                    "                                    </div>" +
                    "                                </div>" +
                    "                            </div>" +
                    "                            " +
                    "                            <p style='margin: 25px 0 0 0; color: rgba(203, 213, 225, 0.8); font-size: 14px; text-align: center; line-height: 1.6;'>" +
                    "                                Si tienes alguna pregunta, nuestro equipo está aquí para ayudarte. 💬" +
                    "                            </p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    " +
                    "                    <!-- Footer mejorado -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 30px; text-align: center; background: rgba(255, 255, 255, 0.02); border-top: 1px solid rgba(255, 255, 255, 0.08);'>" +
                    "                            <p style='margin: 0 0 10px 0; color: #10b981; font-size: 15px; font-weight: 700;'>FlowFit - Tu transformación comienza aquí 🚀</p>" +
                    "                            <p style='margin: 0 0 8px 0; color: #94a3b8; font-size: 13px;'>¿Necesitas ayuda? Contáctanos en <a href='mailto:soporte@flowfit.com' style='color: #10b981; text-decoration: none; font-weight: 600;'>soporte@flowfit.com</a></p>" +
                    "                            <p style='margin: 0 0 12px 0; color: #64748b; font-size: 12px;'>© 2025 FlowFit. Todos los derechos reservados.</p>" +
                    "                            <p style='margin: 0; color: rgba(148, 163, 184, 0.6); font-size: 11px;'>Este es un correo automático, por favor no respondas a este mensaje.</p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                </table>" +
                    "            </td>" +
                    "        </tr>" +
                    "    </table>" +
                    "</body>" +
                    "</html>",
                    nombreUsuario);
        }
    }

    /**
     * Envía correo de aprobación de cuenta con el estilo FlowFit
     */
    public boolean enviarCorreoAprobacion(String destinatario, String nombreUsuario, String tipoUsuario) {
        System.out.println("🔵 [EMAIL] Iniciando envío de correo de aprobación...");
        System.out.println("📧 Destinatario: " + destinatario);
        System.out.println("👤 Nombre: " + nombreUsuario);
        System.out.println("🏷️ Tipo: " + tipoUsuario);
        
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
            System.out.println("📝 Creando mensaje...");
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("✅ ¡Tu cuenta ha sido aprobada en FlowFit!");
            
            System.out.println("📝 Construyendo contenido HTML...");

            String contenido = String.format(
                    "<!DOCTYPE html>" +
                    "<html lang='es'>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "</head>" +
                    "<body style='margin: 0; padding: 0; font-family: \"Segoe UI\", Tahoma, sans-serif; background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%);'>" +
                    "    <table width='100%%' cellpadding='0' cellspacing='0' border='0' style='background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%); padding: 40px 20px;'>" +
                    "        <tr>" +
                    "            <td align='center'>" +
                    "                <table width='600' cellpadding='0' cellspacing='0' border='0' style='max-width: 600px; background: rgba(15, 23, 42, 0.85); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 16px; overflow: hidden; box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);'>" +
                    "                    <!-- Header con Logo - TEMA AZUL ENTRENADOR -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 50px 30px; text-align: center; background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%%, rgba(37, 99, 235, 0.1) 100%%); border-bottom: 1px solid rgba(59, 130, 246, 0.25);'>" +
                    "                            <!-- Logo FlowFit con círculo blanco de fondo -->" +
                    "                            <div style='margin-bottom: 20px;'>" +
                    "                                <div style='display: inline-block; width: 90px; height: 90px; background: #ffffff; border-radius: 50%%; padding: 15px; box-shadow: 0 4px 16px rgba(59, 130, 246, 0.25);'>" +
                    "                                    <img src='cid:flowfitLogo' alt='FlowFit Logo' style='width: 100%%; height: 100%%; object-fit: contain;' />" +
                    "                                </div>" +
                    "                            </div>" +
                    "                            <h1 style='margin: 10px 0 5px 0; color: #3b82f6; font-size: 32px; font-weight: 800; letter-spacing: -0.5px; text-shadow: 0 0 20px rgba(59, 130, 246, 0.4);'>FlowFit</h1>" +
                    "                            <p style='margin: 0; color: rgba(203, 213, 225, 0.8); font-size: 13px; font-weight: 500;'>Sistema de Gestión de Entrenamientos</p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    <!-- Contenido -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 40px 30px;'>" +
                    "                            <!-- Icono de Éxito -->" +
                    "                            <div style='text-align: center; margin-bottom: 30px;'>" +
                    "                                <span style='font-size: 72px; line-height: 1;'>✅</span>" +
                    "                            </div>" +
                    "                            <h2 style='margin: 0 0 20px 0; color: #ffffff; font-size: 24px; font-weight: 700; text-align: center;'>¡Felicidades, %s!</h2>" +
                    "                            <div style='background: linear-gradient(135deg, rgba(59, 130, 246, 0.18) 0%%, rgba(37, 99, 235, 0.12) 100%%); border: 1px solid rgba(59, 130, 246, 0.25); border-radius: 12px; padding: 20px; margin: 25px 0; text-align: center;'>" +
                    "                                <p style='margin: 0; color: #3b82f6; font-size: 17px; font-weight: 700;'>" +
                    "                                    Tu cuenta como %s ha sido aprobada ✅" +
                    "                                </p>" +
                    "                            </div>" +
                    "                            <p style='margin: 20px 0; color: #cbd5e1; font-size: 16px; line-height: 1.6; text-align: center;'>" +
                    "                                Ya puedes iniciar sesión y comenzar a utilizar todas las funcionalidades de la plataforma." +
                    "                            </p>" +
                    "                            <div style='text-align: center; margin: 30px 0;'>" +
                    "                                <a href='http://localhost:8080/login' style='display: inline-block; background: linear-gradient(135deg, #3b82f6 0%%, #2563eb 100%%); color: #ffffff; text-decoration: none; padding: 14px 32px; border-radius: 12px; font-weight: 700; font-size: 15px; box-shadow: 0 8px 24px rgba(59, 130, 246, 0.35);'>Iniciar Sesión Ahora</a>" +
                    "                            </div>" +
                    "                            <p style='margin: 30px 0 0 0; color: rgba(203, 213, 225, 0.8); font-size: 14px; text-align: center; line-height: 1.6;'>" +
                    "                                ¡Te damos la bienvenida al equipo <strong style='color: #3b82f6;'>FlowFit</strong>! 🎉" +
                    "                            </p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    <!-- Footer -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 30px; text-align: center; background: rgba(255, 255, 255, 0.02); border-top: 1px solid rgba(255, 255, 255, 0.08);'>" +
                    "                            <p style='margin: 0 0 10px 0; color: #cbd5e1; font-size: 14px;'>¿Necesitas ayuda?</p>" +
                    "                            <p style='margin: 0 0 20px 0;'><a href='mailto:soporte@flowfit.com' style='color: #3b82f6; text-decoration: none; font-weight: 600;'>soporte@flowfit.com</a></p>" +
                    "                            <p style='margin: 0; color: rgba(148, 163, 184, 0.8); font-size: 12px;'>© 2025 FlowFit. Todos los derechos reservados.</p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                </table>" +
                    "            </td>" +
                    "        </tr>" +
                    "    </table>" +
                    "</body>" +
                    "</html>",
                    nombreUsuario, tipoUsuario);
            
            System.out.println("✉️ Configurando contenido del mensaje con logo...");
            
            // Crear contenedor multipart para adjuntar logo
            MimeMultipart multipart = new MimeMultipart("related");

            // Parte HTML del correo
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(contenido, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // Adjuntar logo como inline
            try {
                MimeBodyPart imagePart = new MimeBodyPart();
                String logoPath = "src/main/resources/static/assets/logo_flowfit.png";
                File logoFile = new File(logoPath);
                
                if (logoFile.exists()) {
                    imagePart.attachFile(logoFile);
                    imagePart.setContentID("<flowfitLogo>");
                    imagePart.setDisposition(MimeBodyPart.INLINE);
                    multipart.addBodyPart(imagePart);
                    System.out.println("📷 Logo adjuntado correctamente");
                } else {
                    System.out.println("⚠️ Logo no encontrado en: " + logoPath);
                }
            } catch (Exception imgEx) {
                System.err.println("⚠️ Error al adjuntar logo (continuando sin logo): " + imgEx.getMessage());
            }

            message.setContent(multipart);
            
            System.out.println("📤 Enviando mensaje vía SMTP...");
            Transport.send(message);
            
            System.out.println("✅ ¡Correo de aprobación enviado exitosamente!");
            return true;
        } catch (MessagingException e) {
            System.err.println("❌ ERROR al enviar correo de aprobación:");
            System.err.println("❌ Tipo de excepción: " + e.getClass().getName());
            System.err.println("❌ Mensaje: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ ERROR INESPERADO al enviar correo de aprobación:");
            System.err.println("❌ Tipo de excepción: " + e.getClass().getName());
            System.err.println("❌ Mensaje: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envía correo de rechazo de cuenta con el estilo FlowFit
     */
    public boolean enviarCorreoRechazo(String destinatario, String nombreUsuario, String tipoUsuario, String motivo) {
        System.out.println("🔴 [EMAIL] Iniciando envío de correo de rechazo...");
        System.out.println("📧 Destinatario: " + destinatario);
        System.out.println("👤 Nombre: " + nombreUsuario);
        System.out.println("🏷️ Tipo: " + tipoUsuario);
        
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
            System.out.println("📝 Creando mensaje...");
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("Actualización sobre tu solicitud en FlowFit");
            
            System.out.println("📝 Construyendo contenido HTML...");


            String motivoHtml = (motivo != null && !motivo.isEmpty())
                    ? "<div style='background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); border-left: 4px solid #ef4444; border-radius: 12px; padding: 20px; margin: 20px 0;'>" +
                      "    <p style='margin: 0 0 8px 0; color: #ef4444; font-size: 14px; font-weight: 600;'>📋 Motivo:</p>" +
                      "    <p style='margin: 0; color: #cbd5e1; font-size: 14px; line-height: 1.6;'>" + motivo + "</p>" +
                      "</div>"
                    : "";

            String contenido = String.format(
                    "<!DOCTYPE html>" +
                    "<html lang='es'>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "</head>" +
                    "<body style='margin: 0; padding: 0; font-family: \"Segoe UI\", Tahoma, sans-serif; background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%);'>" +
                    "    <table width='100%%' cellpadding='0' cellspacing='0' border='0' style='background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%); padding: 40px 20px;'>" +
                    "        <tr>" +
                    "            <td align='center'>" +
                    "                <table width='600' cellpadding='0' cellspacing='0' border='0' style='max-width: 600px; background: rgba(15, 23, 42, 0.85); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 16px; overflow: hidden; box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);'>" +
                    "                    <!-- Header con Logo - TEMA AZUL ENTRENADOR -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 50px 30px; text-align: center; background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%%, rgba(37, 99, 235, 0.05) 100%%); border-bottom: 1px solid rgba(59, 130, 246, 0.2);'>" +
                    "                            <!-- Logo FlowFit con círculo blanco de fondo -->" +
                    "                            <div style='margin-bottom: 20px;'>" +
                    "                                <div style='display: inline-block; width: 90px; height: 90px; background: #ffffff; border-radius: 50%%; padding: 15px; box-shadow: 0 4px 16px rgba(59, 130, 246, 0.25);'>" +
                    "                                    <img src='cid:flowfitLogo' alt='FlowFit Logo' style='width: 100%%; height: 100%%; object-fit: contain;' />" +
                    "                                </div>" +
                    "                            </div>" +
                    "                            <h1 style='margin: 10px 0 5px 0; color: #3b82f6; font-size: 32px; font-weight: 800; letter-spacing: -0.5px; text-shadow: 0 0 20px rgba(59, 130, 246, 0.4);'>FlowFit</h1>" +
                    "                            <p style='margin: 0; color: rgba(203, 213, 225, 0.8); font-size: 13px; font-weight: 500;'>Sistema de Gestión de Entrenamientos</p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    <!-- Contenido -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 40px 30px;'>" +
                    "                            <!-- Icono de Información -->" +
                    "                            <div style='text-align: center; margin-bottom: 30px;'>" +
                    "                                <span style='font-size: 72px; line-height: 1;'>⚠️</span>" +
                    "                            </div>" +
                    "                            <h2 style='margin: 0 0 20px 0; color: #ffffff; font-size: 24px; font-weight: 700; text-align: center;'>Hola, %s</h2>" +
                    "                            <p style='margin: 0 0 20px 0; color: #cbd5e1; font-size: 16px; line-height: 1.6; text-align: center;'>" +
                    "                                Lamentamos informarte que tu solicitud de registro como <strong style='color: #3b82f6;'>%s</strong> no ha sido aprobada en este momento." +
                    "                            </p>" +
                    "                            %s" +
                    "                            <div style='background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%%, rgba(37, 99, 235, 0.05) 100%%); border: 1px solid rgba(59, 130, 246, 0.2); border-radius: 12px; padding: 20px; margin: 25px 0;'>" +
                    "                                <p style='margin: 0 0 10px 0; color: #3b82f6; font-size: 15px; font-weight: 600;'>💬 ¿Tienes dudas?</p>" +
                    "                                <p style='margin: 0; color: #cbd5e1; font-size: 14px; line-height: 1.6;'>" +
                    "                                    Si deseas más información, no dudes en contactarnos. Estamos aquí para ayudarte." +
                    "                                </p>" +
                    "                            </div>" +
                    "                            <div style='text-align: center; margin: 30px 0;'>" +
                    "                                <a href='mailto:soporte@flowfit.com' style='display: inline-block; background: linear-gradient(135deg, #3b82f6 0%%, #2563eb 100%%); color: #ffffff; text-decoration: none; padding: 14px 32px; border-radius: 12px; font-weight: 700; font-size: 15px; box-shadow: 0 8px 24px rgba(59, 130, 246, 0.35);'>Contactar Soporte</a>" +
                    "                            </div>" +
                    "                            <p style='margin: 30px 0 0 0; color: rgba(203, 213, 225, 0.8); font-size: 14px; text-align: center; line-height: 1.6;'>" +
                    "                                Agradecemos tu interés en <strong style='color: #3b82f6;'>FlowFit</strong>." +
                    "                            </p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    <!-- Footer -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 30px; text-align: center; background: rgba(255, 255, 255, 0.02); border-top: 1px solid rgba(255, 255, 255, 0.08);'>" +
                    "                            <p style='margin: 0 0 10px 0; color: #cbd5e1; font-size: 14px;'>Equipo de Soporte</p>" +
                    "                            <p style='margin: 0 0 20px 0;'><a href='mailto:soporte@flowfit.com' style='color: #3b82f6; text-decoration: none; font-weight: 600;'>soporte@flowfit.com</a></p>" +
                    "                            <p style='margin: 0; color: rgba(148, 163, 184, 0.8); font-size: 12px;'>© 2025 FlowFit. Todos los derechos reservados.</p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                </table>" +
                    "            </td>" +
                    "        </tr>" +
                    "    </table>" +
                    "</body>" +
                    "</html>",
                    nombreUsuario, tipoUsuario, motivoHtml);
            
            System.out.println("✉️ Configurando contenido del mensaje con logo...");
            
            // Crear contenedor multipart para adjuntar logo
            MimeMultipart multipart = new MimeMultipart("related");

            // Parte HTML del correo
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(contenido, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // Adjuntar logo como inline
            try {
                MimeBodyPart imagePart = new MimeBodyPart();
                String logoPath = "src/main/resources/static/assets/logo_flowfit.png";
                File logoFile = new File(logoPath);
                
                if (logoFile.exists()) {
                    imagePart.attachFile(logoFile);
                    imagePart.setContentID("<flowfitLogo>");
                    imagePart.setDisposition(MimeBodyPart.INLINE);
                    multipart.addBodyPart(imagePart);
                    System.out.println("📷 Logo adjuntado correctamente");
                } else {
                    System.out.println("⚠️ Logo no encontrado en: " + logoPath);
                }
            } catch (Exception imgEx) {
                System.err.println("⚠️ Error al adjuntar logo (continuando sin logo): " + imgEx.getMessage());
            }

            message.setContent(multipart);
            
            System.out.println("📤 Enviando mensaje vía SMTP...");
            Transport.send(message);
            
            System.out.println("✅ ¡Correo de rechazo enviado exitosamente!");
            return true;
        } catch (MessagingException e) {
            System.err.println("❌ ERROR al enviar correo de rechazo:");
            System.err.println("❌ Tipo de excepción: " + e.getClass().getName());
            System.err.println("❌ Mensaje: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ ERROR INESPERADO al enviar correo de rechazo:");
            System.err.println("❌ Tipo de excepción: " + e.getClass().getName());
            System.err.println("❌ Mensaje: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envía correo para restablecer contraseña
     * @param destinatario Email del destinatario
     * @param nombreUsuario Nombre del usuario
     * @param token Token de reset generado
     * @return true si se envió correctamente
     */
    public boolean enviarCorreoResetPassword(String destinatario, String nombreUsuario, String token) {
        try {
            System.out.println("\n═══════════════════════════════════════════════════");
            System.out.println("📧 ENVIANDO CORREO DE RESET DE CONTRASEÑA");
            System.out.println("═══════════════════════════════════════════════════");
            System.out.println("📬 Destinatario: " + destinatario);
            System.out.println("👤 Nombre: " + nombreUsuario);
            System.out.println("🔑 Token: " + token.substring(0, 10) + "...");
            
            // Configurar propiedades SMTP
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            
            System.out.println("⚙️ Propiedades SMTP configuradas");
            
            // Crear sesión con autenticación
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(REMITENTE, PASSWORD);
                }
            });
            
            System.out.println("🔐 Sesión de correo autenticada");
            
            // Crear mensaje
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE, "FlowFit - Recuperación de Contraseña"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
            message.setSubject("🔐 Restablece tu contraseña - FlowFit", "UTF-8");
            
            System.out.println("📝 Construyendo contenido HTML...");

            // URL del sitio de InfinityFree con el token
            String resetUrl = "http://flowfitpwreset.free.nf/?token=" + token;

            // Construir contenido HTML
            String contenido = 
                    "<!DOCTYPE html>" +
                    "<html lang='es'>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "</head>" +
                    "<body style='margin: 0; padding: 0; font-family: \"Segoe UI\", Tahoma, sans-serif; background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);'>" +
                    "    <table width='100%' cellpadding='0' cellspacing='0' border='0' style='background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%); padding: 40px 20px;'>" +
                    "        <tr>" +
                    "            <td align='center'>" +
                    "                <table width='600' cellpadding='0' cellspacing='0' border='0' style='max-width: 600px; background: rgba(15, 23, 42, 0.85); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 16px; overflow: hidden; box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);'>" +
                    "                    <!-- Header con Logo - TEMA VERDE SEGURIDAD -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 50px 30px; text-align: center; background: linear-gradient(135deg, rgba(16, 185, 129, 0.12) 0%, rgba(16, 185, 129, 0.08) 100%); border-bottom: 1px solid rgba(16, 185, 129, 0.25);'>" +
                    "                            <!-- Logo FlowFit con círculo blanco de fondo -->" +
                    "                            <div style='margin-bottom: 20px;'>" +
                    "                                <div style='display: inline-block; width: 90px; height: 90px; background: #ffffff; border-radius: 50%; padding: 15px; box-shadow: 0 4px 16px rgba(16, 185, 129, 0.25);'>" +
                    "                                    <img src='cid:flowfitLogo' alt='FlowFit Logo' style='width: 100%; height: 100%; object-fit: contain;' />" +
                    "                                </div>" +
                    "                            </div>" +
                    "                            <h1 style='margin: 10px 0 5px 0; color: #10b981; font-size: 32px; font-weight: 800; letter-spacing: -0.5px; text-shadow: 0 0 20px rgba(16, 185, 129, 0.4);'>FlowFit</h1>" +
                    "                            <p style='margin: 0; color: rgba(203, 213, 225, 0.8); font-size: 13px; font-weight: 500;'>Recuperación de Contraseña</p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    <!-- Contenido -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 40px 30px;'>" +
                    "                            <!-- Icono de Seguridad -->" +
                    "                            <div style='text-align: center; margin-bottom: 30px;'>" +
                    "                                <span style='font-size: 72px; line-height: 1;'>🔐</span>" +
                    "                            </div>" +
                    "                            " +
                    "                            <h2 style='margin: 0 0 20px 0; color: #f1f5f9; font-size: 24px; font-weight: 700; text-align: center;'>Hola, " + nombreUsuario + "</h2>" +
                    "                            " +
                    "                            <p style='margin: 0 0 25px 0; color: #cbd5e1; font-size: 15px; line-height: 1.7; text-align: center;'>" +
                    "                                Recibimos una solicitud para restablecer la contraseña de tu cuenta en <strong style='color: #10b981;'>FlowFit</strong>." +
                    "                            </p>" +
                    "                            " +
                    "                            <!-- Tarjeta de información -->" +
                    "                            <div style='background: rgba(16, 185, 129, 0.08); border: 1px solid rgba(16, 185, 129, 0.2); border-left: 4px solid #10b981; border-radius: 12px; padding: 20px; margin: 20px 0;'>" +
                    "                                <p style='margin: 0 0 12px 0; color: #10b981; font-size: 14px; font-weight: 600;'>⚡ Acción requerida:</p>" +
                    "                                <p style='margin: 0; color: #cbd5e1; font-size: 14px; line-height: 1.6;'>" +
                    "                                    Para continuar con el restablecimiento, haz clic en el botón de abajo. Este enlace es <strong>válido por 15 minutos</strong>." +
                    "                                </p>" +
                    "                            </div>" +
                    "                            " +
                    "                            <!-- Botón de acción -->" +
                    "                            <div style='text-align: center; margin: 35px 0 30px 0;'>" +
                    "                                <a href='" + resetUrl + "' style='display: inline-block; background: linear-gradient(135deg, #10b981 0%, #059669 100%); color: #ffffff; padding: 16px 40px; text-decoration: none; border-radius: 12px; font-weight: 700; font-size: 16px; box-shadow: 0 4px 16px rgba(16, 185, 129, 0.3); transition: all 0.3s ease;'>" +
                    "                                    🔓 Restablecer Contraseña" +
                    "                                </a>" +
                    "                            </div>" +
                    "                            " +
                    "                            <!-- Advertencia de seguridad -->" +
                    "                            <div style='background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); border-radius: 12px; padding: 16px; margin: 25px 0;'>" +
                    "                                <p style='margin: 0 0 8px 0; color: #ef4444; font-size: 13px; font-weight: 600;'>⚠️ Importante:</p>" +
                    "                                <p style='margin: 0; color: #cbd5e1; font-size: 13px; line-height: 1.6;'>" +
                    "                                    Si no solicitaste este cambio, ignora este correo. Tu contraseña permanecerá segura." +
                    "                                </p>" +
                    "                            </div>" +
                    "                            " +
                    "                            <!-- Nota técnica -->" +
                    "                            <div style='margin-top: 30px; padding: 20px; background: rgba(59, 130, 246, 0.05); border-radius: 10px; border: 1px solid rgba(59, 130, 246, 0.1);'>" +
                    "                                <p style='margin: 0 0 10px 0; color: #94a3b8; font-size: 12px; line-height: 1.6;'>" +
                    "                                    💡 <strong>¿El botón no funciona?</strong> Copia y pega este enlace en tu navegador:" +
                    "                                </p>" +
                    "                                <p style='margin: 0; color: #3b82f6; font-size: 11px; word-break: break-all; font-family: monospace;'>" +
                    "                                    " + resetUrl +
                    "                                </p>" +
                    "                            </div>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    <!-- Footer -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 30px; text-align: center; background: rgba(15, 23, 42, 0.5); border-top: 1px solid rgba(255, 255, 255, 0.05);'>" +
                    "                            <p style='margin: 0 0 10px 0; color: #64748b; font-size: 13px;'>© 2024 FlowFit - Sistema de Gestión de Entrenamientos</p>" +
                    "                            <p style='margin: 0 0 5px 0; color: #64748b; font-size: 12px;'>📧 Soporte: 0flowfit0@gmail.com</p>" +
                    "                            <p style='margin: 0; color: #475569; font-size: 11px;'>Este correo fue enviado automáticamente, por favor no responder.</p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                </table>" +
                    "            </td>" +
                    "        </tr>" +
                    "    </table>" +
                    "</body>" +
                    "</html>";

            // Crear multipart para email con imagen embebida
            MimeMultipart multipart = new MimeMultipart("related");
            
            // Parte HTML
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(contenido, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);
            
            // Adjuntar logo
            try {
                MimeBodyPart imagePart = new MimeBodyPart();
                String logoPath = "src/main/resources/static/assets/logo_flowfit.png";
                File logoFile = new File(logoPath);
                
                if (logoFile.exists()) {
                    imagePart.attachFile(logoFile);
                    imagePart.setContentID("<flowfitLogo>");
                    imagePart.setDisposition(MimeBodyPart.INLINE);
                    multipart.addBodyPart(imagePart);
                    System.out.println("📷 Logo adjuntado correctamente");
                } else {
                    System.out.println("⚠️ Logo no encontrado en: " + logoPath);
                }
            } catch (Exception imgEx) {
                System.err.println("⚠️ Error al adjuntar logo (continuando sin logo): " + imgEx.getMessage());
            }

            message.setContent(multipart);
            
            System.out.println("📤 Enviando mensaje vía SMTP...");
            Transport.send(message);
            
            System.out.println("✅ ¡Correo de reset de contraseña enviado exitosamente!");
            return true;
        } catch (MessagingException e) {
            System.err.println("❌ ERROR al enviar correo de reset:");
            System.err.println("❌ Tipo de excepción: " + e.getClass().getName());
            System.err.println("❌ Mensaje: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ ERROR INESPERADO al enviar correo de reset:");
            System.err.println("❌ Tipo de excepción: " + e.getClass().getName());
            System.err.println("❌ Mensaje: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envía un boletín (HTML) ya personalizado a un destinatario.
     * Método sencillo reutilizando el JavaMailSender configurado en la app.
     */
    public boolean enviarCorreoBoletin(String destinatario, String nombreUsuario, String asunto, String contenidoHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(REMITENTE);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            // El contenido ya viene como HTML (personalizado)
            helper.setText(contenidoHtml, true);

            mailSender.send(message);
            System.out.println("📧 Boletín enviado a: " + destinatario);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error enviando boletín a " + destinatario + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * ENVÍO MASIVO OPTIMIZADO CON BCC (Copia Oculta)
     * 
     * En lugar de enviar correos uno por uno en un bucle for, este método
     * envía UN SOLO correo con todos los destinatarios en BCC (copia oculta).
     * 
     * Ventajas:
     * - Mucho más rápido (1 envío vs N envíos)
     * - Reduce carga en el servidor SMTP
     * - Los destinatarios no ven los correos de otros (privacidad)
     * - Menos probabilidad de ser bloqueado por spam
     * 
     * IMPORTANTE: Gmail tiene límite de ~500 destinatarios en BCC por día.
     * Para listas más grandes, dividir en lotes.
     * 
     * @param asunto Asunto del correo
     * @param contenidoHtml Contenido HTML del boletín
     * @param destinatarios Array de correos destinatarios
     * @return true si el envío fue exitoso
     */
    public boolean enviarCorreoMasivoBCC(String asunto, String contenidoHtml, String[] destinatarios) {
        try {
            if (destinatarios == null || destinatarios.length == 0) {
                System.err.println("❌ No hay destinatarios para envío masivo");
                return false;
            }
            
            System.out.println("📧 Preparando envío masivo con BCC para " + destinatarios.length + " destinatarios");
            
            // Si hay más de 500 destinatarios, dividir en lotes (límite de Gmail)
            int LOTE_MAXIMO = 500;
            int totalDestinatarios = destinatarios.length;
            int numLotes = (int) Math.ceil((double) totalDestinatarios / LOTE_MAXIMO);
            
            System.out.println("📦 Dividiendo en " + numLotes + " lote(s) de máximo " + LOTE_MAXIMO + " destinatarios");
            
            for (int lote = 0; lote < numLotes; lote++) {
                int inicio = lote * LOTE_MAXIMO;
                int fin = Math.min(inicio + LOTE_MAXIMO, totalDestinatarios);
                
                // Crear sublista para este lote
                String[] destinatariosLote = new String[fin - inicio];
                System.arraycopy(destinatarios, inicio, destinatariosLote, 0, fin - inicio);
                
                System.out.println("📧 Enviando lote " + (lote + 1) + "/" + numLotes + " (" + destinatariosLote.length + " destinatarios)");
                
                // Crear mensaje MIME
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                // Configurar remitente y destinatario visible (el propio remitente)
                helper.setFrom(REMITENTE);
                helper.setTo(REMITENTE); // El destinatario "visible" es el mismo remitente
                
                // Agregar TODOS los destinatarios reales en BCC (Copia Oculta)
                helper.setBcc(destinatariosLote);
                
                // Configurar asunto y contenido
                helper.setSubject(asunto);
                helper.setText(contenidoHtml, true); // true = HTML
                
                // ENVIAR (un solo envío para todos)
                mailSender.send(message);
                
                System.out.println("✅ Lote " + (lote + 1) + " enviado exitosamente");
                
                // Pausa entre lotes para evitar ser bloqueado
                if (lote < numLotes - 1) {
                    Thread.sleep(2000); // 2 segundos entre lotes
                }
            }
            
            System.out.println("✅ Envío masivo completado: " + totalDestinatarios + " destinatarios en " + numLotes + " lote(s)");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error en envío masivo con BCC: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
