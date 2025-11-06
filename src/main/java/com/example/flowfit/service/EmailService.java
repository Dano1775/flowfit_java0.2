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
     * Construye el mensaje HTML de bienvenida con el estilo FlowFit
     */
    private String construirMensajeBienvenida(String nombreUsuario, String tipoUsuario) {
        if ("Entrenador".equals(tipoUsuario) || "Nutricionista".equals(tipoUsuario)) {
            // Email para Entrenador/Nutricionista pendiente de aprobación
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
                    "                    <!-- Header con logo FlowFit -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 40px 30px; text-align: center; background: rgba(255, 255, 255, 0.03); border-bottom: 1px solid rgba(255, 255, 255, 0.1);'>" +
                    "                            <h1 style='margin: 0; color: #4ade80; font-size: 32px; font-weight: 800; letter-spacing: -0.5px; text-shadow: 0 0 20px rgba(74, 222, 128, 0.3);'>FlowFit</h1>" +
                    "                            <p style='margin: 8px 0 0 0; color: rgba(203, 213, 225, 0.8); font-size: 13px; font-weight: 500;'>Sistema de Gestión de Entrenamientos</p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    " +
                    "                    <!-- Contenido principal -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 40px 30px;'>" +
                    "                            <!-- Icono de estado -->" +
                    "                            <div style='text-align: center; margin-bottom: 30px;'>" +
                    "                                <div style='display: inline-block; width: 80px; height: 80px; background: rgba(251, 191, 36, 0.15); border-radius: 16px; line-height: 80px; font-size: 40px;'>⏳</div>" +
                    "                            </div>" +
                    "                            " +
                    "                            <h2 style='margin: 0 0 20px 0; color: #ffffff; font-size: 24px; font-weight: 700; text-align: center;'>¡Hola, %s!</h2>" +
                    "                            " +
                    "                            <p style='margin: 0 0 25px 0; color: #cbd5e1; font-size: 16px; line-height: 1.6; text-align: center;'>" +
                    "                                Tu solicitud de registro como <strong style='color: #4ade80;'>%s</strong> ha sido recibida exitosamente." +
                    "                            </p>" +
                    "                            " +
                    "                            <!-- Tarjeta de información -->" +
                    "                            <div style='background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255, 255, 255, 0.08); border-left: 4px solid #fbbf24; border-radius: 12px; padding: 20px; margin: 25px 0;'>" +
                    "                                <p style='margin: 0 0 12px 0; color: #ffffff; font-size: 15px; font-weight: 600;'>📋 Próximos pasos:</p>" +
                    "                                <p style='margin: 0; color: #cbd5e1; font-size: 14px; line-height: 1.6;'>" +
                    "                                    Un administrador revisará tu solicitud. Te notificaremos por correo cuando tu cuenta sea aprobada. Este proceso suele tomar entre <strong>24-48 horas</strong>." +
                    "                                </p>" +
                    "                            </div>" +
                    "                            " +
                    "                            <p style='margin: 30px 0 0 0; color: rgba(203, 213, 225, 0.8); font-size: 14px; text-align: center; line-height: 1.6;'>" +
                    "                                Gracias por tu interés en formar parte del equipo de <strong style='color: #4ade80;'>FlowFit</strong>. 💪" +
                    "                            </p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    " +
                    "                    <!-- Footer -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 30px; text-align: center; background: rgba(255, 255, 255, 0.02); border-top: 1px solid rgba(255, 255, 255, 0.08);'>" +
                    "                            <p style='margin: 0 0 10px 0; color: #cbd5e1; font-size: 14px;'>¿Tienes preguntas?</p>" +
                    "                            <p style='margin: 0 0 20px 0;'>" +
                    "                                <a href='mailto:soporte@flowfit.com' style='color: #4ade80; text-decoration: none; font-weight: 600;'>soporte@flowfit.com</a>" +
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
            // Email para Usuario (aprobado automáticamente)
            return String.format(
                    "<!DOCTYPE html>" +
                    "<html lang='es'>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "</head>" +
                    "<body style='margin: 0; padding: 0; font-family: \"Segoe UI\", Tahoma, sans-serif; background-color: #1a2332;'>" +
                    "    <table width='100%%' cellpadding='0' cellspacing='0' border='0' style='background-color: #1a2332; padding: 40px 20px;'>" +
                    "        <tr>" +
                    "            <td align='center'>" +
                    "                <table width='600' cellpadding='0' cellspacing='0' border='0' style='max-width: 600px; background-color: #243447; border-radius: 16px; overflow: hidden; box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);'>" +
                    "                    " +
                    "                    <!-- Header Verde Grande con Logo -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 50px 40px; text-align: center; background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); border-radius: 16px 16px 0 0;'>" +
                    "                            <div style='background: rgba(255, 255, 255, 0.2); width: 60px; height: 60px; margin: 0 auto 20px; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center;'>" +
                    "                                <span style='font-size: 32px; line-height: 60px;'>💪</span>" +
                    "                            </div>" +
                    "                            <h1 style='margin: 0 0 10px 0; color: #ffffff; font-size: 36px; font-weight: 700; letter-spacing: -0.5px;'>FlowFit</h1>" +
                    "                            <p style='margin: 0; color: rgba(255, 255, 255, 0.9); font-size: 14px; font-weight: 500;'>Tu centro de entrenamiento personal</p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    " +
                    "                    <!-- Contenido principal -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 40px 40px 30px 40px; background-color: #243447;'>" +
                    "                            " +
                    "                            <!-- Saludo con emoji -->" +
                    "                            <div style='background: linear-gradient(135deg, #2d3e50 0%%, #34495e 100%%); border-left: 4px solid #10b981; border-radius: 12px; padding: 20px 25px; margin-bottom: 30px;'>" +
                    "                                <h2 style='margin: 0; color: #ffffff; font-size: 22px; font-weight: 600;'>¡Hola, %s! 👋</h2>" +
                    "                            </div>" +
                    "                            " +
                    "                            <p style='margin: 0 0 25px 0; color: #cbd5e1; font-size: 16px; line-height: 1.6;'>" +
                    "                                Tu registro ha sido exitoso. ¡Estamos emocionados de acompañarte en tu viaje fitness!" +
                    "                            </p>" +
                    "                            " +
                    "                            <!-- Botón CTA Verde -->" +
                    "                            <div style='text-align: center; margin: 35px 0;'>" +
                    "                                <a href='http://localhost:8080/login' style='display: inline-block; background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); color: #ffffff; text-decoration: none; padding: 16px 40px; border-radius: 10px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);'>✨ Acceder a FlowFit</a>" +
                    "                            </div>" +
                    "                            " +
                    "                            <!-- Tarjeta de Consejo -->" +
                    "                            <div style='background: linear-gradient(135deg, #fbbf24 0%%, #f59e0b 100%%); border-radius: 12px; padding: 20px 25px; margin: 30px 0;'>" +
                    "                                <p style='margin: 0 0 8px 0; color: #1a2332; font-size: 16px; font-weight: 700;'>💡 Consejo del día:</p>" +
                    "                                <p style='margin: 0; color: #1a2332; font-size: 14px; line-height: 1.6;'>" +
                    "                                    La constancia es clave. Pequeños pasos cada día te llevan a grandes resultados." +
                    "                                </p>" +
                    "                            </div>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    " +
                    "                    <!-- Footer -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 30px 40px; text-align: center; background-color: #1a2332; border-radius: 0 0 16px 16px;'>" +
                    "                            <p style='margin: 0 0 15px 0; color: #10b981; font-size: 14px; font-weight: 600;'>FlowFit - Tu transformación comienza aquí</p>" +
                    "                            <p style='margin: 0 0 8px 0; color: #94a3b8; font-size: 13px;'>© 2025 FlowFit. Todos los derechos reservados.</p>" +
                    "                            <p style='margin: 0; color: #64748b; font-size: 12px;'>Este es un correo automático, por favor no respondas a este mensaje.</p>" +
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
            message.setSubject("✅ ¡Tu cuenta ha sido aprobada en FlowFit!");

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
                    "                    <!-- Header -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 40px 30px; text-align: center; background: rgba(74, 222, 128, 0.08); border-bottom: 1px solid rgba(74, 222, 128, 0.2);'>" +
                    "                            <h1 style='margin: 0; color: #4ade80; font-size: 32px; font-weight: 800; letter-spacing: -0.5px; text-shadow: 0 0 20px rgba(74, 222, 128, 0.3);'>FlowFit</h1>" +
                    "                            <p style='margin: 8px 0 0 0; color: rgba(203, 213, 225, 0.8); font-size: 13px; font-weight: 500;'>Sistema de Gestión de Entrenamientos</p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    <!-- Contenido -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 40px 30px;'>" +
                    "                            <div style='text-align: center; margin-bottom: 30px;'>" +
                    "                                <div style='display: inline-block; width: 80px; height: 80px; background: rgba(74, 222, 128, 0.15); border-radius: 16px; line-height: 80px; font-size: 40px;'>✅</div>" +
                    "                            </div>" +
                    "                            <h2 style='margin: 0 0 20px 0; color: #ffffff; font-size: 24px; font-weight: 700; text-align: center;'>¡Felicidades, %s!</h2>" +
                    "                            <div style='background: linear-gradient(135deg, rgba(74, 222, 128, 0.15) 0%%, rgba(34, 197, 94, 0.1) 100%%); border: 1px solid rgba(74, 222, 128, 0.2); border-radius: 12px; padding: 20px; margin: 25px 0; text-align: center;'>" +
                    "                                <p style='margin: 0; color: #4ade80; font-size: 17px; font-weight: 700;'>" +
                    "                                    Tu cuenta como %s ha sido aprobada" +
                    "                                </p>" +
                    "                            </div>" +
                    "                            <p style='margin: 20px 0; color: #cbd5e1; font-size: 16px; line-height: 1.6; text-align: center;'>" +
                    "                                Ya puedes iniciar sesión y comenzar a utilizar todas las funcionalidades de la plataforma." +
                    "                            </p>" +
                    "                            <div style='text-align: center; margin: 30px 0;'>" +
                    "                                <a href='http://localhost:8080/login' style='display: inline-block; background: linear-gradient(135deg, #4ade80 0%%, #22c55e 100%%); color: #0f172a; text-decoration: none; padding: 14px 32px; border-radius: 12px; font-weight: 700; font-size: 15px; box-shadow: 0 8px 24px rgba(74, 222, 128, 0.3);'>Iniciar Sesión Ahora</a>" +
                    "                            </div>" +
                    "                            <p style='margin: 30px 0 0 0; color: rgba(203, 213, 225, 0.8); font-size: 14px; text-align: center; line-height: 1.6;'>" +
                    "                                ¡Te damos la bienvenida al equipo <strong style='color: #4ade80;'>FlowFit</strong>! 🎉" +
                    "                            </p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    <!-- Footer -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 30px; text-align: center; background: rgba(255, 255, 255, 0.02); border-top: 1px solid rgba(255, 255, 255, 0.08);'>" +
                    "                            <p style='margin: 0 0 10px 0; color: #cbd5e1; font-size: 14px;'>¿Necesitas ayuda?</p>" +
                    "                            <p style='margin: 0 0 20px 0;'><a href='mailto:soporte@flowfit.com' style='color: #4ade80; text-decoration: none; font-weight: 600;'>soporte@flowfit.com</a></p>" +
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
            message.setContent(contenido, "text/html; charset=utf-8");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envía correo de rechazo de cuenta con el estilo FlowFit
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
            message.setSubject("Actualización sobre tu solicitud en FlowFit");

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
                    "                    <!-- Header -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 40px 30px; text-align: center; background: rgba(255, 255, 255, 0.03); border-bottom: 1px solid rgba(255, 255, 255, 0.1);'>" +
                    "                            <h1 style='margin: 0; color: #4ade80; font-size: 32px; font-weight: 800; letter-spacing: -0.5px; text-shadow: 0 0 20px rgba(74, 222, 128, 0.3);'>FlowFit</h1>" +
                    "                            <p style='margin: 8px 0 0 0; color: rgba(203, 213, 225, 0.8); font-size: 13px; font-weight: 500;'>Sistema de Gestión de Entrenamientos</p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    <!-- Contenido -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 40px 30px;'>" +
                    "                            <div style='text-align: center; margin-bottom: 30px;'>" +
                    "                                <div style='display: inline-block; width: 80px; height: 80px; background: rgba(239, 68, 68, 0.15); border-radius: 16px; line-height: 80px; font-size: 40px;'>📧</div>" +
                    "                            </div>" +
                    "                            <h2 style='margin: 0 0 20px 0; color: #ffffff; font-size: 24px; font-weight: 700; text-align: center;'>Hola, %s</h2>" +
                    "                            <p style='margin: 0 0 20px 0; color: #cbd5e1; font-size: 16px; line-height: 1.6; text-align: center;'>" +
                    "                                Lamentamos informarte que tu solicitud de registro como <strong style='color: #4ade80;'>%s</strong> no ha sido aprobada en este momento." +
                    "                            </p>" +
                    "                            %s" +
                    "                            <div style='background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 12px; padding: 20px; margin: 25px 0;'>" +
                    "                                <p style='margin: 0 0 10px 0; color: #ffffff; font-size: 15px; font-weight: 600;'>💬 ¿Tienes dudas?</p>" +
                    "                                <p style='margin: 0; color: #cbd5e1; font-size: 14px; line-height: 1.6;'>" +
                    "                                    Si deseas más información, no dudes en contactarnos. Estamos aquí para ayudarte." +
                    "                                </p>" +
                    "                            </div>" +
                    "                            <div style='text-align: center; margin: 30px 0;'>" +
                    "                                <a href='mailto:soporte@flowfit.com' style='display: inline-block; background: linear-gradient(135deg, #4ade80 0%%, #22c55e 100%%); color: #0f172a; text-decoration: none; padding: 14px 32px; border-radius: 12px; font-weight: 700; font-size: 15px; box-shadow: 0 8px 24px rgba(74, 222, 128, 0.3);'>Contactar Soporte</a>" +
                    "                            </div>" +
                    "                            <p style='margin: 30px 0 0 0; color: rgba(203, 213, 225, 0.8); font-size: 14px; text-align: center; line-height: 1.6;'>" +
                    "                                Agradecemos tu interés en <strong style='color: #4ade80;'>FlowFit</strong>." +
                    "                            </p>" +
                    "                        </td>" +
                    "                    </tr>" +
                    "                    <!-- Footer -->" +
                    "                    <tr>" +
                    "                        <td style='padding: 30px; text-align: center; background: rgba(255, 255, 255, 0.02); border-top: 1px solid rgba(255, 255, 255, 0.08);'>" +
                    "                            <p style='margin: 0 0 10px 0; color: #cbd5e1; font-size: 14px;'>Equipo de Soporte</p>" +
                    "                            <p style='margin: 0 0 20px 0;'><a href='mailto:soporte@flowfit.com' style='color: #4ade80; text-decoration: none; font-weight: 600;'>soporte@flowfit.com</a></p>" +
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
            message.setContent(contenido, "text/html; charset=utf-8");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
