package com.example.flowfit.service.notification;

/**
 * Decorador abstracto base para EmailNotification
 * 
 * Esta clase abstracta implementa la interfaz EmailNotification y contiene
 * una referencia al componente envuelto. Todos los decoradores concretos
 * heredarán de esta clase.
 * 
 * Patrón GoF: DECORATOR (Decorador Abstracto)
 * Propósito: Proporcionar una clase base para todos los decoradores concretos
 */
public abstract class EmailNotificationDecorator implements EmailNotification {
    
    protected EmailNotification wrappedNotification;
    
    public EmailNotificationDecorator(EmailNotification notification) {
        this.wrappedNotification = notification;
    }
    
    @Override
    public String getRecipient() {
        return wrappedNotification.getRecipient();
    }
    
    @Override
    public String getSubject() {
        return wrappedNotification.getSubject();
    }
    
    @Override
    public String getContent() {
        return wrappedNotification.getContent();
    }
    
    @Override
    public String getContentType() {
        return wrappedNotification.getContentType();
    }
    
    @Override
    public int getPriority() {
        return wrappedNotification.getPriority();
    }
    
    @Override
    public boolean send() {
        return wrappedNotification.send();
    }
}
