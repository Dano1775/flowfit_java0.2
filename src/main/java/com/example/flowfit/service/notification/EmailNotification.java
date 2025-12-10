package com.example.flowfit.service.notification;

/**
 * Interfaz base para el Patrón Decorator - EmailNotification
 * Esta interfaz define el contrato para todas las notificaciones de email.
 * Permite aplicar el patrón Decorator para agregar funcionalidades dinámicamente
 *xsin modificar la clase base.
 * Patrón GoF: DECORATOR
 * Propósito: Agregar responsabilidades adicionales a un objeto de forma dinámica
 */
public interface EmailNotification {
    
    /**
     * Obtiene el destinatario del email
     * @return dirección de email del destinatario
     */
    String getRecipient();
    
    /**
     * Obtiene el asunto del email
     * @return asunto del email
     */
    String getSubject();
    
    /**
     * Obtiene el contenido/cuerpo del email
     * @return contenido del email
     */
    String getContent();
    
    /**
     * Obtiene el tipo de contenido (text/plain, text/html, etc.)
     * @return tipo MIME del contenido
     */
    String getContentType();
    
    /**
     * Obtiene la prioridad del email
     * @return nivel de prioridad (1=alta, 3=normal, 5=baja)
     */
    int getPriority();
    
    /**
     * Envía la notificación de email
     * @return true si el envío fue exitoso, false en caso contrario
     */
    boolean send();
}
