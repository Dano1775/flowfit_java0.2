# 🧪 Guía de Testing de Pagos (Sin Dinero Real)

## ⚙️ CONFIGURACIÓN INICIAL

### **1. Obtener Credenciales de Prueba**

Ve a: https://www.mercadopago.com.co/developers/panel/credentials

**Pestaña "Credenciales de prueba":**
- Copia el **TEST Access Token** (empieza con `TEST-`)
- Copia el **TEST Public Key** (empieza con `TEST-`)

### **2. Configurar application.properties**

```properties
# Credenciales de PRUEBA (reemplazar con las tuyas)
mercadopago.access-token=TEST-1234567890123456-112233-abc123def456-789012345
mercadopago.public-key=TEST-abc123def-456g-789h-012i-3456jklm7890

# Mantener en modo sandbox
mercadopago.mode=sandbox
app.url=http://localhost:8081
```

---

## 💳 TARJETAS DE PRUEBA

### ✅ **Pago APROBADO** (Escenario Exitoso)
```
Número:     5031 7557 3453 0604
Nombre:     APRO
CVV:        123
Vencimiento: 11/25 (cualquier fecha futura)
DNI/CPF:    12345678
```

### ❌ **Pago RECHAZADO** (Fondos insuficientes)
```
Número:     5031 4332 1540 6351
Nombre:     OTHE
CVV:        123
Vencimiento: 11/25
DNI/CPF:    12345678
```

### ⏳ **Pago PENDIENTE** (Requiere acción manual)
```
Número:     5031 4917 6148 8831
Nombre:     CONT
CVV:        123
Vencimiento: 11/25
DNI/CPF:    12345678
```

### 🔒 **Pago rechazado por seguridad**
```
Número:     5031 7557 3453 0604
Nombre:     CALL
CVV:        123
Vencimiento: 11/25
```

**Más tarjetas:** https://www.mercadopago.com.co/developers/es/docs/testing/test-cards

---

## 🎯 FLUJO DE PRUEBA COMPLETO

### **Paso 1: Crear cuenta de usuario**
```
1. Registro como usuario normal
2. Iniciar sesión
3. Buscar un entrenador
```

### **Paso 2: Iniciar chat con entrenador**
```
1. Ir a página del entrenador
2. Click en "Contactar"
3. Se abre conversación
```

### **Paso 3: Entrenador envía propuesta**
```
1. Login como entrenador
2. Ir a "Mis Conversaciones"
3. Enviar propuesta de plan:
   - Nombre: "Plan Básico"
   - Precio: $100,000 COP
   - Duración: 30 días
   - Rutinas: 4 al mes
   - Seguimiento semanal: Sí
```

### **Paso 4: Usuario negocia (Opcional)**
```
1. Login como usuario
2. Ver propuesta en chat
3. Opciones:
   a) Aceptar y pagar directamente
   b) Hacer contraoferta (cambiar precio/servicios)
   c) El entrenador puede aceptar/contra-contraoferta
```

### **Paso 5: Aceptar propuesta y pagar**
```
1. Click en "Aceptar y Pagar"
2. Redirige a MercadoPago
3. Usar tarjeta de prueba APRO (ver arriba)
4. Completar datos ficticios
5. Confirmar pago
```

### **Paso 6: Verificar estados**

#### En la base de datos:
```sql
-- Ver contratación
SELECT * FROM contratacion_entrenador WHERE id = X;
-- Estado debe ser: ACTIVA

-- Ver pago
SELECT * FROM pago_contratacion WHERE contratacion_id = X;
-- Estado ESCROW debe ser: RETENIDO
-- mp_status debe ser: approved
```

#### En la interfaz:
- Usuario ve en "Mis Entrenamientos": Plan activo
- Entrenador ve en "Mis Usuarios": Nuevo usuario asignado
- Ambos pueden chatear sobre el entrenamiento

---

## 🔍 TESTING DE ESCROW

### **Escenario 1: Servicio completado exitosamente**
```
1. Entrenador asigna rutinas al usuario
2. Transcurren días/semanas
3. Usuario confirma: "Recibí el servicio"
4. Entrenador confirma: "Cumplí con el servicio"
5. RESULTADO: Dinero se libera al entrenador (90% por comisión)
```

Verificar en DB:
```sql
SELECT estado_escrow, monto_liberado_entrenador, fecha_liberacion
FROM pago_contratacion WHERE id = X;
-- estado_escrow = LIBERADO
-- monto_liberado_entrenador = monto_total * 0.90
```

### **Escenario 2: Liberación automática (7 días sin confirmar)**
```
1. Pago completado
2. Nadie confirma nada
3. Esperar 7 días (o cambiar fecha en DB para probar)
4. RESULTADO: Sistema libera automáticamente
```

Cambiar fecha en DB para simular:
```sql
UPDATE pago_contratacion 
SET fecha_retencion_inicio = DATE_SUB(NOW(), INTERVAL 8 DAY)
WHERE id = X;
-- Luego ejecutar tarea programada
```

### **Escenario 3: Disputa (usuario insatisfecho)**
```
1. Usuario no recibe buen servicio
2. Usuario abre disputa desde chat
3. Admin revisa evidencias
4. Admin resuelve:
   a) A favor del usuario → Reembolso
   b) A favor del entrenador → Liberación
```

---

## 🐛 PROBLEMAS COMUNES

### **Error: "Invalid credentials"**
- ✅ Verifica que uses credenciales **TEST-** en modo sandbox
- ✅ Verifica que las credenciales sean de la misma cuenta

### **Pago no se refleja en DB**
- ❌ **PROBLEMA ACTUAL**: No hay endpoints de retorno implementados
- ✅ **SOLUCIÓN**: Necesitas crear `/pagos/success`, `/pagos/failure`, `/pagos/pending`

### **Usuario redirigido pero nada pasa**
- ❌ No hay webhook para recibir notificaciones de MercadoPago
- ✅ Necesitas implementar `/api/webhooks/mercadopago`

---

## 📊 MONITOREO DE PRUEBAS

### **Dashboard de MercadoPago (Sandbox)**
Ve a: https://www.mercadopago.com.co/developers/panel/testing

Podrás ver:
- Pagos de prueba realizados
- Estados de transacciones
- Logs de errores
- Webhooks enviados

### **Base de Datos Local**
```sql
-- Ver últimos pagos
SELECT p.*, c.estado as estado_contratacion
FROM pago_contratacion p
JOIN contratacion_entrenador c ON p.contratacion_id = c.id
ORDER BY p.fecha_creacion DESC
LIMIT 10;

-- Ver pagos retenidos
SELECT * FROM pago_contratacion 
WHERE estado_escrow = 'RETENIDO'
ORDER BY fecha_retencion_inicio DESC;

-- Ver pagos liberados
SELECT * FROM pago_contratacion 
WHERE estado_escrow = 'LIBERADO'
ORDER BY fecha_liberacion DESC;
```

---

## 🚀 SIGUIENTE PASO: IMPLEMENTAR CALLBACKS

**Archivos faltantes que debes crear:**

1. **PagosController.java** - Para manejar retornos de MercadoPago:
   - `GET /pagos/success` - Pago exitoso
   - `GET /pagos/failure` - Pago fallido
   - `GET /pagos/pending` - Pago pendiente

2. **WebhookController.java** - Para notificaciones asíncronas:
   - `POST /api/webhooks/mercadopago` - IPN de MercadoPago

3. **Vistas HTML**:
   - `pagos/success.html` - Página de éxito
   - `pagos/failure.html` - Página de error
   - `pagos/pending.html` - Página de pendiente

¿Quieres que te ayude a implementar estos controladores ahora?
