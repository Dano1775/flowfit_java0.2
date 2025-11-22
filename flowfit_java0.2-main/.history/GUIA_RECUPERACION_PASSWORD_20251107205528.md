# 🔐 SISTEMA DE RECUPERACIÓN DE CONTRASEÑA - FLOWFIT
## Guía Completa de Implementación

---

## 📋 RESUMEN DEL SISTEMA

Este sistema permite a los usuarios restablecer su contraseña mediante un enlace enviado por correo electrónico. El flujo completo es:

1. **Usuario** hace clic en "¿Olvidaste tu contraseña?" en el login
2. **Sistema** genera un token único y lo envía por email
3. **Usuario** hace clic en el enlace del email
4. **InfinityFree** muestra formulario para nueva contraseña
5. **Sistema** valida y cambia la contraseña
6. **Usuario** inicia sesión con nueva contraseña

---

## 🗂️ ARCHIVOS CREADOS

### Backend (Spring Boot):
✅ `model/PasswordResetToken.java` - Entidad JPA para tokens
✅ `repository/PasswordResetTokenRepository.java` - Repositorio de tokens
✅ `service/PasswordResetService.java` - Lógica de negocio
✅ `controller/PasswordResetController.java` - API REST endpoints
✅ `service/EmailService.java` - Nuevo método `enviarCorreoResetPassword()`
✅ `templates/login.html` - Botón y modal de recuperación

### Base de Datos:
✅ `FLOWFIT_DATABASE_COMPLETE.sql` - Script completo con tabla `password_reset_token`

### Frontend (InfinityFree):
✅ `INFINITYFREE_reset.html` - Página de reset de contraseña

---

## 🚀 PASOS PARA IMPLEMENTAR

### PASO 1: Base de Datos

1. Abre **phpMyAdmin** o tu gestor de base de datos MySQL
2. Ejecuta el archivo `FLOWFIT_DATABASE_COMPLETE.sql` completo
3. Verifica que se creó la tabla `password_reset_token`:

```sql
SHOW TABLES;
-- Debe aparecer: password_reset_token

DESCRIBE password_reset_token;
-- Debe mostrar: id, token, usuario_id, fecha_expiracion, fecha_creacion, usado
```

---

### PASO 2: Configurar InfinityFree

#### 2.1 Registrar dominio (si no lo tienes):
1. Ve a https://www.infinityfree.com/
2. Crea una cuenta gratuita
3. Crea un nuevo sitio web
4. Anota tu URL: `https://tudominio.infinityfreeapp.com` o `http://flowfitresetpw.infinityfree.me`

#### 2.2 Subir archivo HTML:
1. Accede al **File Manager** de InfinityFree
2. Ve a la carpeta `htdocs/`
3. Crea una carpeta llamada `reset/` (opcional pero recomendado)
4. Sube el archivo `INFINITYFREE_reset.html`
5. Renómbralo a `reset.html` o `index.html`

#### 2.3 Editar configuración de URL:
Abre `reset.html` en InfinityFree y busca esta línea (línea ~224):

```javascript
const API_BASE_URL = 'http://localhost:8080'; // CAMBIAR ESTO
```

**CÁMBIALA POR:**

```javascript
// Para desarrollo local:
const API_BASE_URL = 'http://localhost:8080';

// Para producción (cuando tengas tu servidor público):
const API_BASE_URL = 'https://tuservidor.com';
```

---

### PASO 3: Actualizar EmailService.java

Abre `src/main/java/com/example/flowfit/service/EmailService.java` y busca la línea **671**:

```java
String resetUrl = "https://flowfitresetpw.infinityfreeapp.com/reset.html?token=" + token;
```

**CÁMBIALA POR TU URL REAL:**

```java
// Ejemplo si tu sitio es http://flowfitresetpw.infinityfree.me
String resetUrl = "http://flowfitresetpw.infinityfree.me/reset.html?token=" + token;

// O si usaste una carpeta:
String resetUrl = "http://flowfitresetpw.infinityfree.me/reset/index.html?token=" + token;
```

---

### PASO 4: Habilitar CORS en Spring Boot

Para que InfinityFree pueda comunicarse con tu servidor, necesitas permitir CORS.

Abre `src/main/java/com/example/flowfit/config/` y busca tu clase de configuración de seguridad (probablemente `SecurityConfig.java` o `WebConfig.java`).

Si no tienes un archivo de configuración CORS, crea uno:

**Opción A: Ya tienes `SecurityConfig.java`**

Agrega este método:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:8080",
        "http://flowfitresetpw.infinityfree.me", // TU URL DE INFINITYFREE
        "https://flowfitresetpw.infinityfreeapp.com" // TU URL ALTERNATIVA
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
}
```

**Opción B: No tienes configuración de CORS**

El controlador ya tiene `@CrossOrigin(origins = "*")` entonces funcionará, pero es menos seguro.

---

### PASO 5: Probar el Sistema Localmente

#### 5.1 Iniciar Spring Boot:
```cmd
mvnw spring-boot:run
```

#### 5.2 Probar el flujo completo:

1. **Abrir login**: http://localhost:8080/login
2. **Hacer clic** en "¿Olvidaste tu contraseña?"
3. **Ingresar email** de un usuario existente (ejemplo: `usuario@flowfit.com`)
4. **Revisar consola** de Spring Boot - deberías ver:
   ```
   📧 ENVIANDO CORREO DE RESET DE CONTRASEÑA
   📬 Destinatario: usuario@flowfit.com
   🔑 Token: abc123XYZ...
   ✅ ¡Correo de reset de contraseña enviado exitosamente!
   ```

5. **Revisar email** - deberás recibir un correo con:
   - Logo FlowFit en círculo blanco
   - Emoji 🔐
   - Botón amarillo "🔓 Restablecer Contraseña"
   - Enlace: `http://flowfitresetpw.infinityfree.me/reset.html?token=...`

6. **Hacer clic en el botón** del email
7. **Ingresar nueva contraseña** (mínimo 6 caracteres)
8. **Confirmar contraseña**
9. **Hacer clic en "Restablecer Contraseña"**
10. **Ver mensaje de éxito** ✅
11. **Ir a login** e iniciar sesión con la nueva contraseña

---

## 🔍 ENDPOINTS API CREADOS

### 1. POST `/api/password-reset/request`
**Descripción**: Solicita un reset de contraseña

**Body**:
```json
{
  "email": "usuario@flowfit.com"
}
```

**Response exitosa**:
```json
{
  "success": true,
  "message": "Si el correo existe, recibirás instrucciones para restablecer tu contraseña."
}
```

---

### 2. GET `/api/password-reset/validate/{token}`
**Descripción**: Valida si un token es válido

**Ejemplo**: `GET /api/password-reset/validate/abc123XYZ456`

**Response si es válido**:
```json
{
  "valid": true,
  "email": "usuario@flowfit.com",
  "nombre": "Juan Pérez",
  "expiraEn": "2024-11-07T15:45:00"
}
```

**Response si es inválido**:
```json
{
  "valid": false,
  "message": "Token inválido o expirado"
}
```

---

### 3. POST `/api/password-reset/change`
**Descripción**: Cambia la contraseña usando el token

**Body**:
```json
{
  "token": "abc123XYZ456",
  "newPassword": "miNuevaPassword123"
}
```

**Response exitosa**:
```json
{
  "success": true,
  "message": "Contraseña actualizada correctamente. Ya puedes iniciar sesión."
}
```

**Response si falla**:
```json
{
  "success": false,
  "message": "Token inválido o expirado"
}
```

---

## 🧪 PROBAR CON POSTMAN/CURL

### Solicitar reset:
```bash
curl -X POST http://localhost:8080/api/password-reset/request \
  -H "Content-Type: application/json" \
  -d '{"email":"usuario@flowfit.com"}'
```

### Validar token:
```bash
curl http://localhost:8080/api/password-reset/validate/ABC123
```

### Cambiar contraseña:
```bash
curl -X POST http://localhost:8080/api/password-reset/change \
  -H "Content-Type: application/json" \
  -d '{"token":"ABC123","newPassword":"nuevapass123"}'
```

---

## ⚙️ CONFIGURACIÓN DE SEGURIDAD

### Tokens:
- ✅ **Generación**: SecureRandom + Base64 (32 bytes = 43 caracteres)
- ✅ **Validez**: 15 minutos desde creación
- ✅ **Un solo uso**: Se marca como `usado=true` al cambiar contraseña
- ✅ **Limpieza**: Tokens anteriores del usuario se eliminan al generar uno nuevo

### Seguridad adicional:
- ✅ No se revela si el email existe o no
- ✅ Token URL-safe (sin caracteres problemáticos)
- ✅ Contraseñas encriptadas con BCrypt
- ✅ Validación de longitud mínima (6 caracteres)

---

## 🐛 SOLUCIÓN DE PROBLEMAS

### Error: "Token inválido o expirado"
**Causa**: El token ya fue usado o pasaron más de 15 minutos
**Solución**: Solicita un nuevo reset de contraseña

### Error: "CORS policy: No 'Access-Control-Allow-Origin' header"
**Causa**: InfinityFree no puede comunicarse con tu servidor
**Solución**: 
1. Verifica que el controlador tenga `@CrossOrigin(origins = "*")`
2. O configura CORS en `SecurityConfig.java` (ver PASO 4)

### Error: "Error de conexión"
**Causa**: Spring Boot no está corriendo o la URL en `reset.html` es incorrecta
**Solución**:
1. Verifica que Spring Boot esté corriendo: `mvnw spring-boot:run`
2. Revisa la consola de JavaScript en el navegador (F12)
3. Verifica que `API_BASE_URL` en `reset.html` apunte a tu servidor

### No llega el email
**Causa**: SMTP de Gmail bloqueando el envío
**Solución**:
1. Verifica que la contraseña de aplicación sea correcta
2. Revisa los logs de Spring Boot - debe decir "✅ Correo enviado"
3. Revisa la carpeta de SPAM
4. Prueba con otro email diferente

---

## 📊 TABLA password_reset_token

```sql
CREATE TABLE password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(100) NOT NULL UNIQUE,          -- Token único
    usuario_id INT NOT NULL,                      -- Usuario que solicitó
    fecha_expiracion DATETIME NOT NULL,           -- Cuando expira (15 min)
    fecha_creacion DATETIME NOT NULL,             -- Cuando se creó
    usado BOOLEAN NOT NULL DEFAULT FALSE,         -- Si ya se usó
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);
```

---

## 📧 EJEMPLO DE EMAIL ENVIADO

**Asunto**: 🔐 Restablece tu contraseña - FlowFit

**Contenido**:
```
┌─────────────────────────────────┐
│   [Logo FlowFit en círculo]     │
│          FlowFit                │
│   Recuperación de Contraseña    │
└─────────────────────────────────┘

              🔐

Hola, Juan Pérez

Recibimos una solicitud para restablecer la 
contraseña de tu cuenta en FlowFit.

┌─────────────────────────────────┐
│ ⚡ Acción requerida:             │
│ Para continuar con el           │
│ restablecimiento, haz clic en   │
│ el botón de abajo. Este enlace  │
│ es válido por 15 minutos.       │
└─────────────────────────────────┘

  [ 🔓 Restablecer Contraseña ]

⚠️ Importante:
Si no solicitaste este cambio, ignora 
este correo. Tu contraseña permanecerá 
segura.

💡 ¿El botón no funciona? Copia y pega:
http://flowfitresetpw.infinityfree.me/reset.html?token=ABC123XYZ
```

---

## ✅ CHECKLIST DE IMPLEMENTACIÓN

### Base de Datos:
- [ ] Ejecutar `FLOWFIT_DATABASE_COMPLETE.sql`
- [ ] Verificar tabla `password_reset_token` existe
- [ ] Verificar foreign key a tabla `usuario`

### InfinityFree:
- [ ] Crear cuenta en InfinityFree
- [ ] Subir archivo `reset.html`
- [ ] Editar `API_BASE_URL` en `reset.html`
- [ ] Probar acceso: `http://tudominio.infinityfree.me/reset.html`

### Spring Boot:
- [ ] Verificar todos los archivos Java creados
- [ ] Actualizar URL en `EmailService.java` línea 671
- [ ] Configurar CORS si es necesario
- [ ] Reiniciar aplicación

### Pruebas:
- [ ] Solicitar reset desde login
- [ ] Verificar email recibido
- [ ] Hacer clic en enlace del email
- [ ] Cambiar contraseña exitosamente
- [ ] Iniciar sesión con nueva contraseña
- [ ] Intentar usar el mismo token (debe fallar)
- [ ] Esperar 15 minutos y probar token expirado

---

## 🎯 TU SIGUIENTE PASO

1. **Ejecuta la base de datos completa** (`FLOWFIT_DATABASE_COMPLETE.sql`)
2. **Sube `reset.html` a InfinityFree**
3. **Actualiza las URLs** (EmailService.java y reset.html)
4. **Reinicia Spring Boot**
5. **Prueba el sistema completo**

---

## 💡 NOTAS IMPORTANTES

### Para Desarrollo:
- Puedes usar `localhost` mientras desarrollas
- InfinityFree puede tardar en reflejar cambios (caché)

### Para Producción:
- Necesitarás un servidor público para Spring Boot
- Opciones: Heroku, AWS, Railway, Render
- Actualiza todas las URLs a las de producción

### Seguridad:
- ❌ NUNCA expongas tu password de Gmail en el código
- ✅ Usa variables de entorno en producción
- ✅ Los tokens son únicos y temporales
- ✅ Las contraseñas se encriptan con BCrypt

---

¿Necesitas ayuda con algún paso específico? ¡Avísame!
