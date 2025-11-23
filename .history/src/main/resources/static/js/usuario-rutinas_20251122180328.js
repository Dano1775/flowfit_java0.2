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
          const nombre = (ej.ejercicioCatalogo && ej.ejercicioCatalogo.nombre) || 'Ejercicio #' + (idx + 1);
          const desc = (ej.ejercicioCatalogo && ej.ejercicioCatalogo.descripcion) || '';
          
          return `
            <div class="ejercicio-item-modal">
              <div class="d-flex gap-3">
                <div class="orden-badge flex-shrink-0" style="width: 32px; height: 32px; background: linear-gradient(135deg, #10b981 0%, #059669 100%); border-radius: 8px; display: flex; align-items: center; justify-content: center; font-weight: 700; color: white; box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);">
                  ${ej.orden || (idx + 1)}
                </div>
                <div class="flex-grow-1">
                  <h6 class="text-white fw-bold mb-2">${nombre}</h6>
                  ${desc ? '<p class="text-muted-flowfit small mb-2">' + desc + '</p>' : ''}
                  <div class="d-flex flex-wrap gap-2">
                    ${ej.series ? '<span class="badge bg-success-flowfit" style="background-color: rgba(16, 185, 129, 0.15) !important; color: #10b981 !important; border: 1px solid rgba(16, 185, 129, 0.3);"><i class="bi bi-repeat me-1"></i>' + ej.series + ' series</span>' : ''}
                    ${ej.repeticiones ? '<span class="badge bg-success-flowfit" style="background-color: rgba(16, 185, 129, 0.15) !important; color: #10b981 !important; border: 1px solid rgba(16, 185, 129, 0.3);"><i class="bi bi-123 me-1"></i>' + ej.repeticiones + ' reps</span>' : ''}
                    ${ej.tiempoSegundos ? '<span class="badge bg-success-flowfit" style="background-color: rgba(16, 185, 129, 0.15) !important; color: #10b981 !important; border: 1px solid rgba(16, 185, 129, 0.3);"><i class="bi bi-stopwatch me-1"></i>' + ej.tiempoSegundos + 's</span>' : ''}
                    ${ej.descansoSegundos ? '<span class="badge bg-secondary-flowfit" style="background-color: rgba(100, 116, 139, 0.15) !important; color: #64748b !important; border: 1px solid rgba(100, 116, 139, 0.3);"><i class="bi bi-pause-circle me-1"></i>' + ej.descansoSegundos + 's descanso</span>' : ''}
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
