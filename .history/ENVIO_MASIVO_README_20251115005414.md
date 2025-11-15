# 📧 Sistema de Envío Masivo - FlowFit

## ✅ Implementación Completa

Se ha implementado un sistema completo de envío masivo de correos con **dos modalidades**:

### 🎯 Características Principales

#### 1. **Mensaje Personalizado**
- ✍️ Escribe mensajes desde cero
- 🔤 Variables dinámicas: `{{nombre}}`, `{{correo}}`, `{{perfil}}`
- 📝 Soporte HTML (etiquetas básicas: `<strong>`, `<p>`, `<ul>`, `<li>`, etc.)
- 👁️ Vista previa en tiempo real
- 📊 Contador de caracteres y destinatarios

#### 2. **Templates Prediseñados** (⚡ Más Rápido)
- 🎉 **Bienvenida** - Para nuevos usuarios
- 🚀 **Nuevas Funcionalidades** - Actualizaciones del sistema
- 💪 **Motivacional** - Mensajes de incentivo
- 🔧 **Mantenimiento** - Notificaciones de downtime
- ⏰ **Recordatorio** - Reactivar usuarios inactivos

### 📂 Archivos Creados/Modificados

#### Backend (Java):
```
✅ src/main/java/com/example/flowfit/controller/BoletinController.java
   - Controlador principal con endpoints para ambas modalidades
   - GET /admin/boletines - Página principal
   - GET /admin/boletines/personalizado - Formulario mensaje personalizado
   - GET /admin/boletines/template - Selector de templates
   - POST /admin/boletines/enviar-personalizado
   - POST /admin/boletines/enviar-template
   - GET /admin/boletines/contar/{tipo} - AJAX contador destinatarios
   - GET /admin/boletines/{id} - Ver detalle de boletín

✅ src/main/java/com/example/flowfit/service/BoletinService.java (ya existía)
   - Lógica de envío asíncrono (@Async)
   - Gestión de estados (PENDIENTE, ENVIANDO, COMPLETADO, FALLIDO)
   - Personalización de contenido con variables
   - Seguimiento de éxitos/fallos

✅ src/main/java/com/example/flowfit/model/BoletinInformativo.java (ya existía)
   - Entidad JPA para historial de boletines
   - Campos: asunto, contenido, tipo destinatario, estado, contadores, fechas
```

#### Frontend (HTML/Thymeleaf):
```
✅ src/main/resources/templates/admin/boletines/index.html
   - Página principal con 2 opciones (Personalizado vs Template)
   - Estadísticas de usuarios activos
   - Historial de boletines enviados
   - Tabla con estado, contadores y fechas

✅ src/main/resources/templates/admin/boletines/personalizado.html
   - Formulario completo para escribir mensaje
   - Selector de destinatarios con contador en tiempo real
   - Variables clickables para insertar en cursor
   - Preview en tiempo real con datos de ejemplo
   - Soporte HTML básico
   - Confirmación antes de enviar

✅ src/main/resources/templates/admin/boletines/template.html
   - Grid de 5 templates prediseñados
   - Vista previa de cada template
   - Envío con un solo click
   - Selector de destinatarios
   - Confirmación rápida

✅ src/main/resources/templates/admin/dashboard.html
   - Agregado enlace en sidebar: "Envío Masivo"
   - Botón en "Acciones Rápidas"
   - Botón grande en sección inferior
```

### 🚀 Cómo Usar

#### **Opción 1: Mensaje Personalizado** (flexible, más lento)
1. Dashboard Admin → **"Envío Masivo"**
2. Click en **"Crear Mensaje"**
3. Seleccionar destinatarios (Todos, Activos, Entrenadores, etc.)
4. Escribir asunto y contenido
5. Usar variables `{{nombre}}`, `{{correo}}`, `{{perfil}}`
6. Previsualizar
7. **Enviar**

**⏱️ Velocidad:** ~500ms por correo (pausa intencional para no saturar SMTP)

#### **Opción 2: Template Prediseñado** (rápido, optimizado)
1. Dashboard Admin → **"Envío Masivo"**
2. Click en **"Usar Template"**
3. Seleccionar destinatarios
4. Elegir un template de la galería
5. (Opcional) Ver vista previa
6. Click en **"Enviar"** del template
7. Confirmar

**⚡ Velocidad:** Optimizado, mismo 500ms/correo pero con HTML pre-renderizado

### 🎯 Tipos de Destinatarios Disponibles

```java
TODOS               → Todos los usuarios registrados
USUARIOS_ACTIVOS    → Solo usuarios con estado "A"
USUARIOS            → Solo perfil "Usuario" (clientes)
ENTRENADORES        → Solo perfil "Entrenador"
NUTRICIONISTAS      → Solo perfil "Nutricionista"
ADMINISTRADORES     → Solo perfil "Administrador"
USUARIOS_INACTIVOS  → Solo usuarios con estado "I"
```

### 📊 Seguimiento y Monitoreo

Cada boletín registra:
- ✅ **Enviados exitosos**
- ❌ **Enviados fallidos**
- 📈 **Total destinatarios**
- 🕐 **Fecha creación**
- 🕑 **Fecha envío**
- 📍 **Estado actual** (Pendiente, Enviando, Completado, Fallido)
- 👤 **Creado por** (administrador que lo envió)

### 🔧 Configuración Técnica

#### Variables Dinámicas Soportadas:
```
{{nombre}}  → Se reemplaza con: usuario.getNombre()
{{correo}}  → Se reemplaza con: usuario.getCorreo()
{{perfil}}  → Se reemplaza con: usuario.getPerfilUsuario().name()
```

#### Envío Asíncrono:
- Usa `@Async` de Spring
- No bloquea la interfaz del usuario
- Actualiza estado en tiempo real
- Reintentos automáticos (configurables)

#### Seguridad:
- ✅ Verificación de sesión administrador
- ✅ Confirmación antes de enviar
- ✅ Registro completo de auditoría
- ✅ Validación de campos obligatorios

### 🎨 Diseño UI/UX

#### Index (Principal):
- 2 cards grandes con iconos
- Estadísticas visuales de usuarios
- Tabla de historial con badges de estado
- Filtros por estado

#### Personalizado:
- Layout 2 columnas (Formulario + Preview)
- Chips de variables clickables
- Preview dinámico con datos de ejemplo
- Contadores en tiempo real

#### Templates:
- Grid responsive de cards
- Hover effects elegantes
- Modal de vista previa
- Envío directo desde cada card

### 📈 Próximas Mejoras (Opcional)

1. **Programación de envíos**
   - Agendar fecha/hora de envío
   - Envíos recurrentes (semanales, mensuales)

2. **Editor HTML WYSIWYG**
   - Editor visual tipo Quill o TinyMCE
   - Drag & drop de imágenes

3. **Estadísticas avanzadas**
   - Tasa de apertura (requiere tracking pixel)
   - Clicks en enlaces (requiere URL tracking)
   - Gráficas de rendimiento

4. **Segmentación avanzada**
   - Por fechas de registro
   - Por nivel de actividad
   - Por progreso en rutinas

5. **A/B Testing**
   - Probar 2 versiones de un mensaje
   - Enviar la mejor al resto

### 🐛 Troubleshooting

#### "No puedo acceder a /admin/boletines"
- Verificar que estés logueado como administrador
- Session debe tener `perfil_usuario = "Administrador"`

#### "El envío es muy lento"
- Es intencional (500ms pausa entre correos)
- Para reducir, modificar `Thread.sleep(500)` en `BoletinService.java`
- ⚠️ No quitar completamente o el servidor SMTP puede rechazar

#### "Las variables no se reemplazan"
- Verificar sintaxis: `{{nombre}}` (dobles llaves)
- Case-sensitive: `{{Nombre}}` NO funciona
- Solo soportadas: `{{nombre}}`, `{{correo}}`, `{{perfil}}`

#### "El contador de destinatarios no funciona"
- Revisar consola del navegador (F12)
- Endpoint debe responder JSON: `/admin/boletines/contar/{tipo}`
- Verificar que `UsuarioRepository` tenga los métodos `countBy...`

### ✅ Testing Recomendado

1. **Test con pocos destinatarios primero**
   - Crear usuario de prueba
   - Enviar solo a "Administradores" (1 usuario)
   - Verificar recepción de correo

2. **Verificar variables**
   - Template "Bienvenida" es ideal para testing
   - Revisar que {{nombre}} se reemplace correctamente

3. **Revisar historial**
   - Verificar que contadores actualicen
   - Estado debe cambiar: PENDIENTE → ENVIANDO → COMPLETADO

4. **Logs del servidor**
   - `BoletinService.java` imprime logs detallados
   - Ver consola para seguir progreso en tiempo real

---

## 🎉 ¡Listo para Usar!

El sistema está completamente funcional. Accede desde:

**Dashboard Admin → Envío Masivo** (botón en sidebar o acciones rápidas)

O directo: `http://localhost:8080/admin/boletines`

---

**Desarrollado para FlowFit** 💪
*Noviembre 2025*
