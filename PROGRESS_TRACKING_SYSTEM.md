# Sistema de Seguimiento de Progreso - FlowFit

## 📊 Resumen de Implementación

Se ha implementado un **sistema completo de seguimiento de progreso de ejercicios** para reemplazar los datos estáticos de la página de progreso del usuario.

## 🔧 Archivos Creados/Modificados

### Backend (Java/Spring Boot)

#### 1. **ProgresoEjercicio.java** - Entidad JPA
- **Ubicación**: `src/main/java/com/example/flowfit/model/ProgresoEjercicio.java`
- **Propósito**: Mapea la tabla `progreso_ejercicio` de la base de datos
- **Campos**:
  - `id` (Integer) - Identificador único
  - `usuario` (Usuario) - Usuario que realizó el ejercicio
  - `rutinaAsignada` (RutinaAsignada) - Rutina a la que pertenece
  - `ejercicio` (EjercicioCatalogo) - Ejercicio realizado
  - `fecha` (LocalDate) - Fecha del progreso
  - `seriesCompletadas` (Integer) - Número de series realizadas
  - `repeticionesRealizadas` (Integer) - Repeticiones por serie
  - `pesoUtilizado` (Double) - Peso utilizado en kg
  - `tiempoSegundos` (Integer) - Duración del ejercicio
  - `comentarios` (String) - Notas adicionales

#### 2. **ProgresoEjercicioRepository.java** - Repositorio
- **Ubicación**: `src/main/java/com/example/flowfit/repository/ProgresoEjercicioRepository.java`
- **Métodos personalizados**:
  - `findByUsuarioOrderByFechaDesc` - Historial del usuario
  - `findByUsuarioAndFechaBetween` - Progreso en un rango de fechas
  - `countByUsuario` - Total de ejercicios completados
  - `findByUsuarioAndEjercicioId` - Evolución de un ejercicio específico
  - `countDiasEntrenadosUltimaSemana` - Días de entrenamiento última semana
  - `sumSeriesCompletadasDesde` - Total de series desde una fecha
  - `avgPesoUtilizadoByEjercicio` - Promedio de peso por ejercicio
  - `getEstadisticasPorFecha` - Datos agrupados por fecha para gráficas

#### 3. **ProgresoService.java** - Capa de Servicio
- **Ubicación**: `src/main/java/com/example/flowfit/service/ProgresoService.java`
- **Métodos principales**:
  - `registrarProgreso()` - Registrar nuevo progreso de ejercicio
  - `getEstadisticasGenerales()` - Estadísticas del usuario (ejercicios totales, días entrenados, racha)
  - `getDatosGraficas()` - Datos para gráficas de Chart.js
  - `getEvolucionPeso()` - Evolución de peso en un ejercicio específico
  - `calcularRachaActual()` - Calcula días consecutivos de entrenamiento
  - `getUltimoProgreso()` - Últimas entradas de progreso
  - `getEjerciciosMasRealizados()` - Top ejercicios más frecuentes

#### 4. **UsuarioController.java** - Endpoints REST (MODIFICADO)
- **Nuevos endpoints**:
  - `GET /usuario/progreso/estadisticas` - Estadísticas generales
  - `GET /usuario/progreso/graficas?dias=7` - Datos para gráficas (7, 30, 90 días)
  - `GET /usuario/progreso/ejercicio/{ejercicioId}` - Evolución de un ejercicio
  - `POST /usuario/progreso/registrar` - Registrar nuevo progreso
  - `GET /usuario/progreso/top-ejercicios?limite=5` - Ejercicios más realizados

### Frontend (HTML/JavaScript)

#### 5. **progreso.html** - Vista del Usuario (MODIFICADO)
- **Ubicación**: `src/main/resources/templates/usuario/progreso.html`
- **Cambios**:
  - **Tarjetas de estadísticas**: Actualizadas con atributos `data-stat` para actualización dinámica
    - `data-stat="total-ejercicios"` - Ejercicios totales
    - `data-stat="dias-entrenados"` - Días entrenados última semana
    - `data-stat="total-series"` - Series completadas última semana
    - `data-stat="racha-actual"` - Racha de días consecutivos 🔥
  
  - **JavaScript actualizado**:
    - `cargarEstadisticas()` - Fetch de estadísticas desde API
    - `cargarGraficas(dias)` - Fetch de datos para gráficas
    - `actualizarGraficoProgreso(datos)` - Renderiza gráfica con Chart.js
    - `cambiarPeriodoGrafico(dias)` - Cambiar período (7, 30, 90 días)
    - `cargarTopEjercicios()` - Top ejercicios más realizados

  - **Gráficas con Chart.js**:
    - Gráfica de líneas con **dos ejes Y**: ejercicios (verde) y series (azul)
    - Gráfica de dona: distribución de rutinas completadas vs en progreso
    - Interactividad completa con tooltips y leyendas

## 🎨 Características Implementadas

### ✅ Estadísticas en Tiempo Real
- Total de ejercicios completados
- Días entrenados en la última semana
- Total de series completadas última semana
- Racha actual de días consecutivos de entrenamiento

### 📈 Gráficas Dinámicas
- **Gráfica de Progreso Semanal**: Muestra ejercicios y series por día
- **Gráfica de Distribución**: Estado de rutinas (completadas vs en progreso)
- **Períodos ajustables**: 7, 30 o 90 días

### 🔢 Cálculos Inteligentes
- **Racha consecutiva**: Detecta si entrenó hoy o ayer para mantener la racha
- **Agrupación por fecha**: Datos agrupados para gráficas limpias
- **Evolución de peso**: Seguimiento del progreso de carga en ejercicios

## 🚀 Próximos Pasos Sugeridos

### 1. Probar el Sistema
```bash
# Iniciar la aplicación
mvnw.cmd spring-boot:run

# Abrir en navegador
http://localhost:8081/usuario/progreso
```

### 2. Registrar Progreso de Prueba
Para probar el sistema, necesitas registrar progreso desde la vista de rutinas del usuario o directamente con el endpoint:

```javascript
// Ejemplo de registro de progreso
POST /usuario/progreso/registrar
Content-Type: application/x-www-form-urlencoded

rutinaAsignadaId=1&ejercicioId=5&series=3&repeticiones=12&peso=50.5&comentarios=Buen+entrenamiento
```

### 3. Verificar Datos en Base de Datos
```sql
-- Ver progreso registrado
SELECT * FROM progreso_ejercicio ORDER BY fecha DESC LIMIT 10;

-- Ver estadísticas de un usuario
SELECT 
    u.nombre,
    COUNT(*) as total_ejercicios,
    SUM(series_completadas) as total_series,
    AVG(peso_utilizado) as peso_promedio
FROM progreso_ejercicio pe
JOIN usuario u ON pe.usuario_id = u.id
WHERE u.id = 1
GROUP BY u.id, u.nombre;
```

### 4. Agregar Formulario de Registro
Considera agregar un botón/modal en la vista de progreso para que el usuario pueda registrar ejercicios manualmente:

```html
<!-- Botón para registrar progreso -->
<button class="btn btn-usuario" data-bs-toggle="modal" data-bs-target="#registrarProgresoModal">
  <i class="bi bi-plus-circle me-2"></i>Registrar Progreso
</button>
```

### 5. Integrar con Rutinas
Cuando el usuario complete un ejercicio de su rutina asignada, automáticamente registrar el progreso:

```java
// En RutinaService o similar
progresoService.registrarProgreso(usuario, rutinaAsignadaId, ejercicioId, 
    series, repeticiones, peso, comentarios);
```

## 📋 Estructura de la Base de Datos

La tabla `progreso_ejercicio` ya existe en tu base de datos con esta estructura:

```sql
CREATE TABLE progreso_ejercicio (
    id INT PRIMARY KEY AUTO_INCREMENT,
    usuario_id INT NOT NULL,
    rutina_asignada_id INT,
    ejercicio_id INT NOT NULL,
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    series_completadas INT,
    repeticiones_realizadas INT,
    peso_utilizado DECIMAL(5,2),
    tiempo_segundos INT,
    comentarios TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (rutina_asignada_id) REFERENCES rutina_asignada(id),
    FOREIGN KEY (ejercicio_id) REFERENCES ejercicio_catalogo(id),
    INDEX idx_usuario_fecha (usuario_id, fecha),
    INDEX idx_rutina_asignada (rutina_asignada_id)
);
```

## 🎯 Beneficios del Sistema

1. **Datos Reales**: Ya no hay datos simulados o estáticos
2. **Seguimiento Preciso**: Cada ejercicio queda registrado con todos sus detalles
3. **Motivación**: La racha de días consecutivos motiva al usuario a entrenar diariamente
4. **Análisis**: Las gráficas permiten ver tendencias y progreso a lo largo del tiempo
5. **Escalable**: Fácil agregar más métricas (calorías, RM, volumen total, etc.)

## 🔍 Testing

### Endpoints REST
```bash
# Estadísticas generales
curl http://localhost:8081/usuario/progreso/estadisticas

# Datos para gráficas (últimos 30 días)
curl http://localhost:8081/usuario/progreso/graficas?dias=30

# Evolución de un ejercicio
curl http://localhost:8081/usuario/progreso/ejercicio/5

# Top ejercicios
curl http://localhost:8081/usuario/progreso/top-ejercicios?limite=10
```

## 📝 Notas Técnicas

- **Chart.js**: Versión incluida desde CDN, no requiere npm install
- **Formato de fechas**: ISO 8601 (YYYY-MM-DD) desde el backend
- **Colores**: Mantiene el esquema verde del usuario (#10b981, #059669)
- **Responsive**: Las gráficas se adaptan a diferentes tamaños de pantalla
- **Performance**: Las consultas están optimizadas con índices en la base de datos

---

**Estado**: ✅ Sistema implementado y compilado sin errores
**Compilación**: ✅ Exitosa con `mvnw.cmd clean compile`
**Próximo paso**: Iniciar aplicación y probar endpoints

💪 ¡El sistema de progreso está listo para trackear tus entrenamientos!
