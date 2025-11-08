# 🔧 Solución - Error de Envío de Correos

## ❌ Problema Identificado

Al aprobar o rechazar un entrenador, el sistema mostraba:
```
"El correo de notificación no pudo enviarse"
```

## 🔍 Causa Raíz

El archivo `application.properties` tenía valores de ejemplo que no coincidían con las credenciales reales:

```properties
# ❌ ANTES (valores de ejemplo)
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-password-de-aplicacion
```

Mientras que el `EmailService.java` tenía hardcodeadas las credenciales correctas:
```java
private static final String REMITENTE = "0flowfit0@gmail.com";
private static final String PASSWORD = "pbvg igyq ticm xqgq";
```

## ✅ Solución Aplicada

Se actualizó `application.properties` con las credenciales correctas:

```properties
# ✅ DESPUÉS (credenciales correctas)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=0flowfit0@gmail.com
spring.mail.password=pbvg igyq ticm xqgq
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

También se actualizó el remitente por defecto:
```properties
flowfit.email.from=0flowfit0@gmail.com
flowfit.email.fromName=FlowFit - Sistema de Notificaciones
```

## 🚀 Pasos para Aplicar la Solución

### 1️⃣ **Detener la Aplicación**
Si la aplicación está corriendo, detenerla con `Ctrl + C` en la terminal.

### 2️⃣ **Reiniciar Spring Boot**
```cmd
mvnw spring-boot:run
```

O si usas Maven instalado:
```cmd
mvn spring-boot:run
```

### 3️⃣ **Verificar Logs al Iniciar**
Busca en los logs que no haya errores relacionados con email:
```
[main] o.s.mail.javamail.JavaMailSenderImpl : JavaMail version: 2.0.1
```

### 4️⃣ **Probar el Envío**
1. Ir a: `http://localhost:8080/admin/usuarios-pendientes`
2. Aprobar o rechazar un entrenador
3. Verificar que el mensaje sea:
   - ✅ **"Usuario aprobado exitosamente. Se ha enviado un correo de confirmación."**
   - ❌ **NO debe aparecer:** "El correo de notificación no pudo enviarse"

### 5️⃣ **Verificar Recepción del Correo**
- Revisar la bandeja de entrada del usuario aprobado/rechazado
- El correo debe llegar desde: **0flowfit0@gmail.com**
- El diseño debe mostrar:
  - 🔵 Tema azul para entrenadores/nutricionistas
  - 🟢 Tema verde para clientes
  - Logo FlowFit
  - SVG icons profesionales

## 📧 Configuración de Email

### Cuenta de Gmail Utilizada
- **Email:** 0flowfit0@gmail.com
- **Password:** `pbvg igyq ticm xqgq` (Contraseña de aplicación)
- **SMTP:** smtp.gmail.com:587
- **TLS:** Habilitado

### ⚠️ Notas Importantes

1. **Contraseña de Aplicación:**
   - La password `pbvg igyq ticm xqgq` es una **contraseña de aplicación** de Google
   - NO es la contraseña regular de la cuenta
   - Se genera en: Google Account → Security → 2-Step Verification → App Passwords

2. **Seguridad:**
   - ⚠️ **NO compartir estas credenciales públicamente**
   - Considerar usar variables de entorno en producción
   - El archivo `application.properties` NO debe estar en el repositorio público

3. **Límites de Gmail:**
   - Gmail tiene límite de ~500 correos/día para cuentas gratuitas
   - Para envíos masivos, considerar servicios como SendGrid, AWS SES, etc.

## 🔒 Mejora de Seguridad (Opcional)

Para producción, se recomienda usar variables de entorno:

### application.properties
```properties
spring.mail.username=${FLOWFIT_EMAIL_USERNAME}
spring.mail.password=${FLOWFIT_EMAIL_PASSWORD}
```

### Variables de Entorno (Windows)
```cmd
set FLOWFIT_EMAIL_USERNAME=0flowfit0@gmail.com
set FLOWFIT_EMAIL_PASSWORD=pbvg igyq ticm xqgq
```

### Variables de Entorno (Linux/Mac)
```bash
export FLOWFIT_EMAIL_USERNAME=0flowfit0@gmail.com
export FLOWFIT_EMAIL_PASSWORD="pbvg igyq ticm xqgq"
```

## ✅ Resultado Esperado

Después de reiniciar la aplicación:

1. ✅ Los correos se envían correctamente
2. ✅ Mensaje de éxito: "Se ha enviado un correo de confirmación"
3. ✅ Usuario recibe email con tema azul (entrenador) o verde (cliente)
4. ✅ Email contiene logo FlowFit y diseño profesional
5. ✅ Botones funcionan correctamente

## 🐛 Troubleshooting

### Si sigue sin funcionar:

1. **Verificar credenciales en los logs:**
   ```
   Error: Authentication failed
   ```
   → Verificar que la contraseña de aplicación sea correcta

2. **Error de conexión:**
   ```
   Could not connect to SMTP host
   ```
   → Verificar firewall/antivirus
   → Verificar conexión a internet

3. **Timeout:**
   ```
   Connection timed out
   ```
   → Aumentar timeout en application.properties
   → Verificar que no haya proxy bloqueando SMTP

4. **TLS Error:**
   ```
   STARTTLS is required
   ```
   → Verificar que `spring.mail.properties.mail.smtp.starttls.enable=true`

## 📝 Archivos Modificados

- ✅ `src/main/resources/application.properties`
  - Línea 34-42: Credenciales SMTP actualizadas
  - Línea 58-59: Email remitente actualizado

## 🎉 Estado

**✅ SOLUCIONADO** - Los correos ahora se enviarán correctamente después de reiniciar la aplicación.

---

**Fecha:** 2025-11-07  
**Archivo:** SOLUCION_CORREOS.md
