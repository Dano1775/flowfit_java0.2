// FlowFit - Usuario Rutinas JavaScript
console.log('✅ usuario-rutinas.js cargado');

// Auto-hide alerts after 5 seconds
setTimeout(function() {
  const alerts = document.querySelectorAll('.alert');
  alerts.forEach(alert => {
    if (alert.classList.contains('show')) {
      const bsAlert = new bootstrap.Alert(alert);
      bsAlert.close();
    }
  });
}, 5000);

// Smooth scroll to recommended routines
document.addEventListener('DOMContentLoaded', function() {
  const exploreBtn = document.querySelector('a[href="#rutinas-recomendadas"]');
  if (exploreBtn) {
    exploreBtn.addEventListener('click', function(e) {
      e.preventDefault();
      document.getElementById('rutinas-recomendadas').scrollIntoView({
        behavior: 'smooth'
      });
    });
  }
});

// Confirm completion of routine
document.addEventListener('DOMContentLoaded', function() {
  document.querySelectorAll('form[action*="completar"] button').forEach(button => {
    button.addEventListener('click', function(e) {
      if (!confirm('¿Estás seguro de que has completado esta rutina?')) {
        e.preventDefault();
      }
    });
  });
});

// ===== MODAL DETALLES DE RUTINA =====
function abrirModalDetalles(rutinaId) {
  console.log('🔍 Abriendo modal para rutina ID:', rutinaId);
  
  const modalElement = document.getElementById('rutinaDetallesModal');
  if (!modalElement) {
    console.error('❌ Modal no encontrado');
    return;
  }
  
  // Resetear estado
  document.getElementById('modalLoading').classList.remove('d-none');
  document.getElementById('modalError').classList.add('d-none');
  document.getElementById('modalContenido').classList.add('d-none');
  
  // Abrir modal
  const modal = new bootstrap.Modal(modalElement);
  modal.show();
  
  // Cargar datos
  fetch('/usuario/api/rutinas/' + rutinaId)
    .then(response => {
      console.log('📡 Response status:', response.status);
      if (!response.ok) {
        throw new Error('Error HTTP: ' + response.status);
      }
      return response.json();
    })
    .then(data => {
      console.log('✅ Datos recibidos:', data);
      
      // Ocultar loading
      document.getElementById('modalLoading').classList.add('d-none');
      document.getElementById('modalContenido').classList.remove('d-none');
      
      const rutina = data.rutina;
      const ejercicios = data.ejercicios || [];
      const rutinaAsignada = data.rutinaAsignada || null;
      
      console.log('✅ Datos recibidos:', data);
      console.log('📋 Ejercicios:', ejercicios);
      ejercicios.forEach((ej, idx) => {
        console.log(`Ejercicio ${idx + 1}:`, {
          nombre: ej.ejercicioNombre,
          imagen: ej.ejercicioImagen,
          descripcion: ej.ejercicioDescripcion
        });
      });
      
      // Información básica del header
      document.getElementById('modalRutinaNombre').textContent = rutina.nombre || 'Sin nombre';
      document.getElementById('modalRutinaDescripcion').textContent = rutina.descripcion || 'Sin descripción';
      document.getElementById('modalRutinaEjerciciosCount').textContent = ejercicios.length;
      document.getElementById('modalRutinaDuracion').textContent = rutina.duracionMinutos || 0;
      
      // Agregar información adicional de la rutina
      const infoAdicional = document.createElement('div');
      infoAdicional.className = 'mb-4';
      infoAdicional.innerHTML = `
        <div class="row g-3 mb-4">
          ${rutina.dificultad ? '<div class="col-6 col-md-3"><div class="text-center p-3 rounded-3" style="background: rgba(251, 146, 60, 0.1); border: 1px solid rgba(251, 146, 60, 0.2);"><i class="bi bi-speedometer2 d-block mb-2" style="font-size: 1.5rem; color: #fb923c;"></i><small class="text-muted d-block" style="font-size: 0.75rem;">Dificultad</small><strong class="text-white">' + rutina.dificultad + '</strong></div></div>' : ''}
          ${rutina.categoria ? '<div class="col-6 col-md-3"><div class="text-center p-3 rounded-3" style="background: rgba(168, 85, 247, 0.1); border: 1px solid rgba(168, 85, 247, 0.2);"><i class="bi bi-tag d-block mb-2" style="font-size: 1.5rem; color: #a855f7;"></i><small class="text-muted d-block" style="font-size: 0.75rem;">Categoría</small><strong class="text-white">' + rutina.categoria + '</strong></div></div>' : ''}
          ${rutina.caloriasEstimadas ? '<div class="col-6 col-md-3"><div class="text-center p-3 rounded-3" style="background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2);"><i class="bi bi-fire d-block mb-2" style="font-size: 1.5rem; color: #ef4444;"></i><small class="text-muted d-block" style="font-size: 0.75rem;">Calorías</small><strong class="text-white">' + rutina.caloriasEstimadas + ' kcal</strong></div></div>' : ''}
          ${rutina.objetivo ? '<div class="col-6 col-md-3"><div class="text-center p-3 rounded-3" style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2);"><i class="bi bi-bullseye d-block mb-2" style="font-size: 1.5rem; color: #10b981;"></i><small class="text-muted d-block" style="font-size: 0.75rem;">Objetivo</small><strong class="text-white">' + rutina.objetivo + '</strong></div></div>' : ''}
        </div>
      `;
      
      // Insertar la info adicional al inicio del contenido modal
      const listaEjercicios = document.getElementById('modalEjerciciosLista');
      listaEjercicios.insertAdjacentElement('beforebegin', infoAdicional);
      
      // Lista de ejercicios con el estilo de entrenador adaptado a verde
      if (ejercicios.length === 0) {
        listaEjercicios.innerHTML = `
          <div class="text-center py-5" style="color: rgba(255, 255, 255, 0.6);">
            <i class="bi bi-inbox" style="font-size: 4rem; color: rgba(251, 146, 60, 0.3); margin-bottom: 1.5rem; display: block;"></i>
            <h5 class="text-white mb-3">No hay ejercicios</h5>
            <p class="mb-0">Esta rutina aún no tiene ejercicios asignados.</p>
          </div>
        `;
      } else {
        listaEjercicios.innerHTML = ejercicios.map((ej, idx) => {
          // Usar campos directos del EjercicioRutinaDto
          const nombre = ej.ejercicioNombre || 'Ejercicio #' + (idx + 1);
          const desc = ej.ejercicioDescripcion || '';
          const grupoMuscular = ''; // No disponible en el DTO actual
          const dificultad = ''; // No disponible en el DTO actual
          
          // Construir la ruta de la imagen
          let imagenUrl = '';
          if (ej.ejercicioImagen) {
            // Si ya tiene la ruta completa (http o /), usarla tal cual
            if (ej.ejercicioImagen.startsWith('http') || ej.ejercicioImagen.startsWith('/')) {
              imagenUrl = ej.ejercicioImagen;
            } else {
              // Si no, construir la ruta completa
              imagenUrl = '/ejercicio_image_uploads/' + ej.ejercicioImagen;
            }
            console.log('🖼️ Imagen construida:', imagenUrl, 'Original:', ej.ejercicioImagen);
          }
          
          return `
            <div class="mb-3" style="background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255, 255, 255, 0.1); border-left: 4px solid #10b981; border-radius: 12px; padding: 1.5rem; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); position: relative; overflow: hidden;">
              <div class="d-flex gap-3">
                <!-- Número de orden -->
                <div class="flex-shrink-0" style="width: 40px; height: 40px; background: linear-gradient(135deg, #10b981 0%, #059669 100%); border-radius: 10px; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 1.125rem; color: white; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);">
                  ${ej.orden || (idx + 1)}
                </div>
                
                <!-- Contenido -->
                <div class="flex-grow-1">
                  <div class="d-flex align-items-start justify-content-between mb-2">
                    <h6 class="text-white fw-bold mb-0">${nombre}</h6>
                    ${grupoMuscular || dificultad ? '<div class="d-flex gap-2">' + 
                      (grupoMuscular ? '<span class="badge" style="background: rgba(16, 185, 129, 0.15); color: #10b981; border: 1px solid rgba(16, 185, 129, 0.3); font-size: 0.75rem;">' + grupoMuscular + '</span>' : '') +
                      (dificultad ? '<span class="badge" style="background: rgba(251, 146, 60, 0.15); color: #fb923c; border: 1px solid rgba(251, 146, 60, 0.3); font-size: 0.75rem;">' + dificultad + '</span>' : '') +
                    '</div>' : ''}
                  </div>
                  
                  ${desc ? '<p class="text-white small mb-3" style="line-height: 1.6; opacity: 0.9;">' + desc + '</p>' : ''}
                  
                  <!-- Imagen del ejercicio - Estilo completo y elegante -->
                  ${imagenUrl ? `
                    <div class="mb-3" style="max-width: 300px;">
                      <img src="${imagenUrl}" alt="${nombre}" 
                           style="width: 100%; 
                                  height: auto; 
                                  max-height: 250px;
                                  object-fit: contain; 
                                  border-radius: 8px; 
                                  border: 2px solid rgba(16, 185, 129, 0.3);
                                  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
                                  background: rgba(0, 0, 0, 0.2);" 
                           onerror="console.error('Error cargando imagen:', this.src); this.parentElement.style.display='none'">
                    </div>
                  ` : ''}
                  
                  <!-- Stats -->
                  <div class="d-flex flex-wrap gap-2">
                    ${ej.series ? '<span class="badge" style="background: rgba(16, 185, 129, 0.15); color: #10b981; border: 1px solid rgba(16, 185, 129, 0.3);"><i class="bi bi-repeat me-1"></i>' + ej.series + ' series</span>' : ''}
                    ${ej.repeticiones ? '<span class="badge" style="background: rgba(16, 185, 129, 0.15); color: #10b981; border: 1px solid rgba(16, 185, 129, 0.3);"><i class="bi bi-arrow-repeat me-1"></i>' + ej.repeticiones + ' reps</span>' : ''}
                    ${ej.duracionSegundos ? '<span class="badge" style="background: rgba(100, 116, 139, 0.15); color: #94a3b8; border: 1px solid rgba(100, 116, 139, 0.3);"><i class="bi bi-clock me-1"></i>' + ej.duracionSegundos + 's</span>' : ''}
                    ${ej.descansoSegundos ? '<span class="badge" style="background: rgba(100, 116, 139, 0.15); color: #94a3b8; border: 1px solid rgba(100, 116, 139, 0.3);"><i class="bi bi-pause-circle me-1"></i>Descanso: ' + ej.descansoSegundos + 's</span>' : ''}
                    ${ej.pesoKg ? '<span class="badge" style="background: rgba(251, 191, 36, 0.15); color: #fbbf24; border: 1px solid rgba(251, 191, 36, 0.3);"><i class="bi bi-lightning me-1"></i>' + ej.pesoKg + ' kg</span>' : ''}
                  </div>
                  
                  <!-- Notas -->
                  ${ej.notas ? '<div class="mt-2"><small class="text-white" style="opacity: 0.8;"><i class="bi bi-sticky me-1"></i>' + ej.notas + '</small></div>' : ''}
                </div>
              </div>
            </div>
          `;
        }).join('');
      }
      
      // Progreso (solo si hay rutinaAsignada)
      const progresoContainer = document.getElementById('modalProgresoContainer');
      if (rutinaAsignada && rutinaAsignada.progreso !== null && rutinaAsignada.progreso !== undefined) {
        progresoContainer.classList.remove('d-none');
        const progreso = Math.round(rutinaAsignada.progreso);
        document.getElementById('modalProgreso').textContent = progreso + '%';
        const progressBar = document.getElementById('modalProgresoBar');
        progressBar.style.width = progreso + '%';
        progressBar.setAttribute('aria-valuenow', progreso);
      } else {
        progresoContainer.classList.add('d-none');
      }
      
      // Botón de completar (solo si hay rutinaAsignada)
      const completarBtn = document.getElementById('modalCompletarBtn');
      if (rutinaAsignada && rutinaAsignada.id) {
        completarBtn.classList.remove('d-none');
        completarBtn.onclick = function() {
          completarRutina(rutinaAsignada.id);
        };
      } else {
        completarBtn.classList.add('d-none');
      }
      
      // Botón de descargar PDF
      const descargarPdfBtn = document.getElementById('modalDescargarPdfBtn');
      descargarPdfBtn.onclick = function() {
        descargarRutinaPDF(rutinaId, rutina, ejercicios);
      };
    })
    .catch(error => {
      console.error('❌ Error al cargar detalles:', error);
      document.getElementById('modalLoading').classList.add('d-none');
      document.getElementById('modalError').classList.remove('d-none');
    });
}

// Exponer función globalmente
window.abrirModalDetalles = abrirModalDetalles;

function descargarRutinaPDF(rutinaId, rutina, ejercicios) {
  console.log('📥 Generando PDF para rutina:', rutinaId);
  
  // Crear contenido HTML para el PDF
  let contenidoPDF = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>${rutina.nombre} - FlowFit</title>
      <style>
        body {
          font-family: Arial, sans-serif;
          margin: 40px;
          color: #333;
        }
        .header {
          text-align: center;
          margin-bottom: 30px;
          border-bottom: 3px solid #10b981;
          padding-bottom: 20px;
        }
        .header h1 {
          color: #10b981;
          margin: 0;
          font-size: 32px;
        }
        .header p {
          color: #666;
          margin: 10px 0;
        }
        .info-section {
          background: #f8f9fa;
          padding: 20px;
          border-radius: 8px;
          margin-bottom: 30px;
        }
        .info-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 15px;
          margin-top: 15px;
        }
        .info-item {
          background: white;
          padding: 12px;
          border-radius: 6px;
          border-left: 3px solid #10b981;
        }
        .info-label {
          font-size: 12px;
          color: #666;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }
        .info-value {
          font-size: 18px;
          font-weight: bold;
          color: #333;
          margin-top: 4px;
        }
        .ejercicio {
          margin-bottom: 25px;
          border: 1px solid #e5e7eb;
          border-left: 4px solid #10b981;
          border-radius: 8px;
          padding: 20px;
          break-inside: avoid;
        }
        .ejercicio-header {
          display: flex;
          align-items: center;
          margin-bottom: 15px;
        }
        .ejercicio-numero {
          background: linear-gradient(135deg, #10b981, #059669);
          color: white;
          width: 40px;
          height: 40px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: bold;
          font-size: 18px;
          margin-right: 15px;
        }
        .ejercicio-nombre {
          font-size: 20px;
          font-weight: bold;
          color: #333;
        }
        .ejercicio-descripcion {
          color: #666;
          margin-bottom: 15px;
          line-height: 1.6;
        }
        .ejercicio-stats {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
        }
        .stat {
          background: #f0fdf4;
          border: 1px solid #10b981;
          padding: 8px 12px;
          border-radius: 6px;
          font-size: 14px;
          color: #059669;
        }
        .footer {
          text-align: center;
          margin-top: 40px;
          padding-top: 20px;
          border-top: 2px solid #e5e7eb;
          color: #666;
          font-size: 12px;
        }
      </style>
    </head>
    <body>
      <div class="header">
        <h1>🏋️ ${rutina.nombre}</h1>
        <p>${rutina.descripcion || 'Sin descripción'}</p>
      </div>
      
      <div class="info-section">
        <h3 style="margin-top: 0; color: #10b981;">📊 Información de la Rutina</h3>
        <div class="info-grid">
          <div class="info-item">
            <div class="info-label">Duración</div>
            <div class="info-value">${rutina.duracionMinutos || 0} min</div>
          </div>
          <div class="info-item">
            <div class="info-label">Ejercicios</div>
            <div class="info-value">${ejercicios.length}</div>
          </div>
          <div class="info-item">
            <div class="info-label">Dificultad</div>
            <div class="info-value">${rutina.dificultad || 'N/A'}</div>
          </div>
          <div class="info-item">
            <div class="info-label">Categoría</div>
            <div class="info-value">${rutina.categoria || 'N/A'}</div>
          </div>
          <div class="info-item">
            <div class="info-label">Calorías</div>
            <div class="info-value">${rutina.caloriasEstimadas || 0} kcal</div>
          </div>
          <div class="info-item">
            <div class="info-label">Objetivo</div>
            <div class="info-value">${rutina.objetivo || 'N/A'}</div>
          </div>
        </div>
      </div>
      
      <h3 style="color: #10b981; margin-bottom: 20px;">💪 Ejercicios</h3>
  `;
  
  // Agregar ejercicios
  ejercicios.forEach((ej, idx) => {
    const nombre = ej.ejercicioNombre || 'Ejercicio #' + (idx + 1);
    const desc = ej.ejercicioDescripcion || '';
    
    contenidoPDF += `
      <div class="ejercicio">
        <div class="ejercicio-header">
          <div class="ejercicio-numero">${ej.orden || (idx + 1)}</div>
          <div class="ejercicio-nombre">${nombre}</div>
        </div>
        ${desc ? '<div class="ejercicio-descripcion">' + desc + '</div>' : ''}
        <div class="ejercicio-stats">
          ${ej.series ? '<div class="stat">🔄 ' + ej.series + ' series</div>' : ''}
          ${ej.repeticiones ? '<div class="stat">🔁 ' + ej.repeticiones + ' reps</div>' : ''}
          ${ej.duracionSegundos ? '<div class="stat">⏱️ ' + ej.duracionSegundos + 's</div>' : ''}
          ${ej.descansoSegundos ? '<div class="stat">⏸️ Descanso: ' + ej.descansoSegundos + 's</div>' : ''}
          ${ej.pesoKg ? '<div class="stat">⚡ ' + ej.pesoKg + ' kg</div>' : ''}
        </div>
        ${ej.notas ? '<div style="margin-top: 12px; padding: 10px; background: #fef3c7; border-left: 3px solid #fbbf24; border-radius: 4px;"><strong>📝 Nota:</strong> ' + ej.notas + '</div>' : ''}
      </div>
    `;
  });
  
  contenidoPDF += `
      <div class="footer">
        <p><strong>FlowFit</strong> - Tu plataforma de entrenamiento personalizado</p>
        <p>Generado el ${new Date().toLocaleDateString('es-ES', { year: 'numeric', month: 'long', day: 'numeric' })}</p>
      </div>
    </body>
    </html>
  `;
  
  // Crear ventana temporal para imprimir
  const ventanaImpresion = window.open('', '_blank');
  ventanaImpresion.document.write(contenidoPDF);
  ventanaImpresion.document.close();
  
  // Esperar a que cargue y luego mostrar el diálogo de impresión
  ventanaImpresion.onload = function() {
    ventanaImpresion.focus();
    ventanaImpresion.print();
  };
}

function completarRutina(rutinaAsignadaId) {
  if (confirm('¿Estás seguro de que has completado esta rutina?')) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/usuario/rutinas/completar';
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'rutinaAsignadaId';
    input.value = rutinaAsignadaId;
    form.appendChild(input);
    document.body.appendChild(form);
    form.submit();
  }
}

function agregarRutina(rutinaId) {
  if (confirm('¿Deseas agregar esta rutina a tu plan de entrenamiento?')) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/usuario/rutinas/asignar';
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'rutinaId';
    input.value = rutinaId;
    form.appendChild(input);
    document.body.appendChild(form);
    form.submit();
  }
}

window.completarRutina = completarRutina;
window.agregarRutina = agregarRutina;
