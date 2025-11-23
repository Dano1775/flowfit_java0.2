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
      
      // Información básica
      document.getElementById('modalRutinaNombre').textContent = rutina.nombre || 'Sin nombre';
      document.getElementById('modalRutinaDescripcion').textContent = rutina.descripcion || 'Sin descripción';
      document.getElementById('modalRutinaEjerciciosCount').textContent = ejercicios.length;
      document.getElementById('modalRutinaDuracion').textContent = rutina.duracionMinutos || 0;
      
      // Lista de ejercicios
      const listaEjercicios = document.getElementById('modalEjerciciosLista');
      if (ejercicios.length === 0) {
        listaEjercicios.innerHTML = '<p class="text-muted-flowfit text-center py-3 mb-0"><i class="bi bi-inbox me-2"></i>No hay ejercicios en esta rutina</p>';
      } else {
        listaEjercicios.innerHTML = ejercicios.map((ej, idx) => {
          const ejercicio = ej.ejercicioCatalogo || {};
          const nombre = ejercicio.nombre || 'Ejercicio #' + (idx + 1);
          const desc = ejercicio.descripcion || '';
          const imagenUrl = ejercicio.imagenUrl || '/ejercicio_image_uploads/default_ejercicio.jpg';
          const grupoMuscular = ejercicio.grupoMuscular || 'General';
          const dificultad = ejercicio.dificultad || 'Media';
          
          return `
            <div class="ejercicio-item-modal mb-3" style="background: rgba(15, 23, 42, 0.6); border: 1px solid rgba(16, 185, 129, 0.25); border-radius: 16px; overflow: hidden; transition: all 0.3s ease;">
              <div class="row g-0">
                <div class="col-md-4">
                  <img src="${imagenUrl}" 
                       alt="${nombre}" 
                       style="width: 100%; height: 200px; object-fit: cover;"
                       onerror="this.src='/ejercicio_image_uploads/default_ejercicio.jpg'">
                </div>
                <div class="col-md-8">
                  <div class="p-3">
                    <div class="d-flex align-items-start justify-content-between mb-2">
                      <div>
                        <div class="d-flex align-items-center gap-2 mb-2">
                          <span style="display: inline-flex; align-items: center; justify-content: center; width: 32px; height: 32px; background: linear-gradient(135deg, #10b981 0%, #059669 100%); border-radius: 8px; font-weight: 700; color: white; font-size: 0.875rem; box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);">
                            ${ej.orden || (idx + 1)}
                          </span>
                          <h6 class="text-white fw-bold mb-0" style="font-size: 1.1rem;">${nombre}</h6>
                        </div>
                        ${desc ? '<p class="text-muted-flowfit small mb-3" style="line-height: 1.6;">' + desc + '</p>' : ''}
                      </div>
                      <div class="d-flex gap-2">
                        <span class="badge" style="background: rgba(16, 185, 129, 0.15); color: #10b981; border: 1px solid rgba(16, 185, 129, 0.3); font-size: 0.75rem;">
                          ${grupoMuscular}
                        </span>
                        <span class="badge" style="background: rgba(251, 146, 60, 0.15); color: #fb923c; border: 1px solid rgba(251, 146, 60, 0.3); font-size: 0.75rem;">
                          ${dificultad}
                        </span>
                      </div>
                    </div>
                    
                    <div class="row g-2 mb-2">
                      ${ej.series ? '<div class="col-6 col-lg-3"><div class="d-flex align-items-center gap-2 p-2 rounded" style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2);"><i class="bi bi-repeat" style="color: #10b981; font-size: 1.25rem;"></i><div><small class="text-muted d-block" style="font-size: 0.7rem;">Series</small><strong class="text-white" style="font-size: 0.9rem;">' + ej.series + '</strong></div></div></div>' : ''}
                      ${ej.repeticiones ? '<div class="col-6 col-lg-3"><div class="d-flex align-items-center gap-2 p-2 rounded" style="background: rgba(59, 130, 246, 0.1); border: 1px solid rgba(59, 130, 246, 0.2);"><i class="bi bi-arrow-repeat" style="color: #3b82f6; font-size: 1.25rem;"></i><div><small class="text-muted d-block" style="font-size: 0.7rem;">Repeticiones</small><strong class="text-white" style="font-size: 0.9rem;">' + ej.repeticiones + '</strong></div></div></div>' : ''}
                      ${ej.duracionSegundos ? '<div class="col-6 col-lg-3"><div class="d-flex align-items-center gap-2 p-2 rounded" style="background: rgba(168, 85, 247, 0.1); border: 1px solid rgba(168, 85, 247, 0.2);"><i class="bi bi-stopwatch" style="color: #a855f7; font-size: 1.25rem;"></i><div><small class="text-muted d-block" style="font-size: 0.7rem;">Duración</small><strong class="text-white" style="font-size: 0.9rem;">' + ej.duracionSegundos + 's</strong></div></div></div>' : ''}
                      ${ej.descansoSegundos ? '<div class="col-6 col-lg-3"><div class="d-flex align-items-center gap-2 p-2 rounded" style="background: rgba(100, 116, 139, 0.1); border: 1px solid rgba(100, 116, 139, 0.2);"><i class="bi bi-pause-circle" style="color: #64748b; font-size: 1.25rem;"></i><div><small class="text-muted d-block" style="font-size: 0.7rem;">Descanso</small><strong class="text-white" style="font-size: 0.9rem;">' + ej.descansoSegundos + 's</strong></div></div></div>' : ''}
                      ${ej.pesoKg ? '<div class="col-6 col-lg-3"><div class="d-flex align-items-center gap-2 p-2 rounded" style="background: rgba(251, 191, 36, 0.1); border: 1px solid rgba(251, 191, 36, 0.2);"><i class="bi bi-lightning-fill" style="color: #fbbf24; font-size: 1.25rem;"></i><div><small class="text-muted d-block" style="font-size: 0.7rem;">Peso</small><strong class="text-white" style="font-size: 0.9rem;">' + ej.pesoKg + ' kg</strong></div></div></div>' : ''}
                    </div>
                    
                    ${ej.notas ? '<div class="mt-2 p-2 rounded" style="background: rgba(251, 191, 36, 0.1); border-left: 3px solid #fbbf24;"><small class="text-warning"><i class="bi bi-lightbulb-fill me-1"></i><strong>Nota:</strong> ' + ej.notas + '</small></div>' : ''}
                  </div>
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
    })
    .catch(error => {
      console.error('❌ Error al cargar detalles:', error);
      document.getElementById('modalLoading').classList.add('d-none');
      document.getElementById('modalError').classList.remove('d-none');
    });
}

// Exponer función globalmente
window.abrirModalDetalles = abrirModalDetalles;

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
