(function () {
  function initUsuarioCalendar() {
    var calendarEl = document.getElementById('calendarUsuario');
    if (!calendarEl || typeof FullCalendar === 'undefined') return;

    var modalEl = document.getElementById('modalEventoRutina');
    var modal = null;
    if (modalEl && window.bootstrap) {
      modal = new bootstrap.Modal(modalEl);
    }

    var modalGeneration = 0;

    function showDayDetail(info) {
      var myGen = ++modalGeneration;
      if (!modalEl) return;

      var eventObj = info.event;
      var startStr = eventObj.startStr;
      var ext = eventObj.extendedProps || {};
      var rutinaAsignadaId = ext.rutinaAsignadaId;
      var estado = ext.estado || '';
      var esDescanso = ext.esDescanso;
      var programada = ext.programada;
      var diaNombre = ext.diaNombre || '';

      // Set header
      var titleEl = modalEl.querySelector('[data-ff-title]');
      var dateEl = modalEl.querySelector('[data-ff-date]');
      if (titleEl) titleEl.textContent = eventObj.title || 'Rutina';
      if (dateEl) dateEl.textContent = startStr;

      // Set day badge
      var diaNombreEl = modalEl.querySelector('[data-ff-dia-nombre]');
      if (diaNombreEl) diaNombreEl.textContent = diaNombre || 'Sin programar';

      // Set estado badge
      var estadoBadge = modalEl.querySelector('[data-ff-estado-badge]');
      var stateEl = modalEl.querySelector('[data-ff-state]');
      if (stateEl) {
        if (programada === false) stateEl.textContent = 'Sin programar';
        else if (esDescanso) stateEl.textContent = 'Descanso';
        else stateEl.textContent = estado;
      }
      if (estadoBadge) {
        if (estado === 'REALIZADA') {
          estadoBadge.style.background = 'rgba(16, 185, 129, 0.2)';
          estadoBadge.style.color = '#10b981';
          estadoBadge.style.border = '1px solid rgba(16, 185, 129, 0.3)';
        } else if (estado === 'CANCELADA' || programada === false) {
          estadoBadge.style.background = 'rgba(100, 116, 139, 0.2)';
          estadoBadge.style.color = '#94a3b8';
          estadoBadge.style.border = '1px solid rgba(100, 116, 139, 0.3)';
        } else {
          estadoBadge.style.background = 'rgba(59, 130, 246, 0.2)';
          estadoBadge.style.color = '#60a5fa';
          estadoBadge.style.border = '1px solid rgba(59, 130, 246, 0.3)';
        }
      }

      // Reset sections
      var loading = document.getElementById('modalDiaLoading');
      var descanso = document.getElementById('modalDiaDescanso');
      var ejercicios = document.getElementById('modalDiaEjercicios');
      var sinEjercicios = document.getElementById('modalDiaSinEjercicios');
      var progresoSection = document.getElementById('modalDiaProgreso');
      var btnIniciarEl = document.getElementById('btnIniciarSesion');

      if (loading) loading.classList.remove('d-none');
      if (descanso) descanso.classList.add('d-none');
      if (ejercicios) ejercicios.classList.add('d-none');
      if (sinEjercicios) sinEjercicios.classList.add('d-none');
      if (progresoSection) progresoSection.classList.add('d-none');
      if (btnIniciarEl) { btnIniciarEl.classList.add('d-none'); btnIniciarEl.setAttribute('href', '#'); }

      if (modal) modal.show();

      // If rest day, show rest message
      if (esDescanso) {
        if (loading) loading.classList.add('d-none');
        if (descanso) descanso.classList.remove('d-none');
        return;
      }

      // If not programmed or no assignment
      if (!rutinaAsignadaId || programada === false) {
        if (loading) loading.classList.add('d-none');
        if (sinEjercicios) sinEjercicios.classList.remove('d-none');
        return;
      }

      // Hide completion button initially
      var btnCompletar = document.getElementById('btnCompletarDia');
      if (btnCompletar) btnCompletar.classList.add('d-none');

      // Fetch day detail from API
      fetch('/usuario/api/calendario/dia-detalle?rutinaAsignadaId=' + rutinaAsignadaId + '&fecha=' + encodeURIComponent(startStr))
        .then(function (r) { return r.json(); })
        .then(function (data) {
          if (myGen !== modalGeneration) return;
          if (loading) loading.classList.add('d-none');

          if (data.error) {
            if (sinEjercicios) sinEjercicios.classList.remove('d-none');
            return;
          }

          var ejList = data.ejercicios || [];
          if (ejList.length === 0) {
            if (sinEjercicios) sinEjercicios.classList.remove('d-none');
            return;
          }

          // Store context for completion
          var currentRutinaAsignadaId = data.rutinaAsignadaId || rutinaAsignadaId;
          var currentFecha = startStr;
          var allCompleted = data.ejerciciosCompletados === ejList.length;

          // Show exercises
          if (ejercicios) ejercicios.classList.remove('d-none');
          var countEl = document.getElementById('modalDiaEjerciciosCount');
          if (countEl) {
            var completedCount = data.ejerciciosCompletados || 0;
            countEl.textContent = completedCount + '/' + ejList.length + ' completados';
            if (allCompleted) {
              countEl.style.background = 'rgba(16, 185, 129, 0.3)';
            } else {
              countEl.style.background = 'rgba(16, 185, 129, 0.2)';
            }
          }

          var lista = document.getElementById('modalDiaEjerciciosLista');
          if (!lista) return;

          // Build all HTML first, then set once — avoids DOM destruction on each += iteration
          var htmlStr = '';
          ejList.forEach(function (ej, idx) {
            var imgSrc = ej.imagen ? ('/ejercicio_image_uploads/' + ej.imagen) : null;
            var isCompleted = ej.completado === true;
            var badges = '';
            if (ej.series) badges += '<span class="badge" style="background:rgba(59,130,246,0.2);color:#60a5fa;padding:0.3rem 0.6rem;font-size:0.7rem;border-radius:6px;border:1px solid rgba(59,130,246,0.3);">' + ej.series + ' series</span>';
            if (ej.repeticiones) badges += '<span class="badge" style="background:rgba(168,85,247,0.2);color:#a855f7;padding:0.3rem 0.6rem;font-size:0.7rem;border-radius:6px;border:1px solid rgba(168,85,247,0.3);">' + ej.repeticiones + ' reps</span>';
            if (ej.pesoKg) badges += '<span class="badge" style="background:rgba(251,146,60,0.2);color:#fb923c;padding:0.3rem 0.6rem;font-size:0.7rem;border-radius:6px;border:1px solid rgba(251,146,60,0.3);">' + ej.pesoKg + ' kg</span>';
            if (ej.duracionSegundos) badges += '<span class="badge" style="background:rgba(16,185,129,0.2);color:#10b981;padding:0.3rem 0.6rem;font-size:0.7rem;border-radius:6px;border:1px solid rgba(16,185,129,0.3);"><i class="bi bi-clock me-1"></i>' + Math.round(ej.duracionSegundos / 60) + ' min</span>';
            if (ej.descansoSegundos) badges += '<span class="badge" style="background:rgba(100,116,139,0.2);color:#94a3b8;padding:0.3rem 0.6rem;font-size:0.7rem;border-radius:6px;border:1px solid rgba(100,116,139,0.3);"><i class="bi bi-pause-circle me-1"></i>' + ej.descansoSegundos + 's desc</span>';

            var notasHtml = ej.notas ? '<p class="mb-0 mt-2" style="font-size:0.8rem;color:#94a3b8;font-style:italic;"><i class="bi bi-chat-text me-1"></i>' + ej.notas + '</p>' : '';

            var html = '<div class="ejercicio-item-modal d-flex align-items-start gap-3' + (isCompleted ? ' completado' : '') + '">';
            // Checkbox
            html += '<input type="checkbox" class="ejercicio-check" data-ejercicio-id="' + ej.ejercicioId + '" data-series="' + (ej.series || 0) + '" data-repeticiones="' + (ej.repeticiones || 0) + '" data-peso="' + (ej.pesoKg || 0) + '"' + (isCompleted ? ' checked' : '') + '>';
            html += '<div class="ej-num-badge flex-shrink-0"><span>' + (idx + 1) + '</span></div>';
            if (imgSrc) {
              html += '<img src="' + imgSrc + '" alt="" style="width:52px;height:52px;object-fit:cover;border-radius:10px;border:1px solid rgba(255,255,255,0.1);" class="flex-shrink-0">';
            }
            html += '<div class="ejercicio-info flex-grow-1">';
            html += '<h6 class="text-white mb-1 fw-bold" style="font-size:0.95rem;">' + (ej.nombre || 'Ejercicio') + '</h6>';
            if (badges) html += '<div class="d-flex flex-wrap gap-1 mb-1">' + badges + '</div>';
            if (ej.descripcion) html += '<p class="mb-0" style="font-size:0.8rem;color:#cbd5e1;line-height:1.4;">' + ej.descripcion + '</p>';
            html += notasHtml;
            html += '</div></div>';

            htmlStr += html;
          });
          lista.innerHTML = htmlStr;

          // Show/hide completion button
          if (btnCompletar && !allCompleted) {
            btnCompletar.classList.remove('d-none');
          }

          // Click on exercise row toggles checkbox (assigned to property to avoid accumulation)
          lista.onclick = function (e) {
            var item = e.target.closest('.ejercicio-item-modal');
            if (!item) return;
            var check = item.querySelector('.ejercicio-check');
            if (e.target === check || !check) return;
            check.checked = !check.checked;
            check.dispatchEvent(new Event('change', { bubbles: true }));
          };

          // Update completed style, button, and day-progress bar on checkbox change
          lista.onchange = function (e) {
            if (e.target.classList.contains('ejercicio-check')) {
              var item = e.target.closest('.ejercicio-item-modal');
              if (item) {
                if (e.target.checked) item.classList.add('completado');
                else item.classList.remove('completado');
              }
              updateCompletarButton();
              updateDayProgress();
            }
          };

          // Show day-completion progress bar (exercises done today / total today)
          var progresoVal = document.getElementById('modalDiaProgresoVal');
          var progresoBar = document.getElementById('modalDiaProgresoBar');

          function updateDayProgress() {
            var allChecks = lista.querySelectorAll('.ejercicio-check');
            var checkedNow = lista.querySelectorAll('.ejercicio-check:checked').length;
            var pctDia = allChecks.length > 0 ? Math.round((checkedNow / allChecks.length) * 100) : 0;
            if (progresoSection) progresoSection.classList.remove('d-none');
            if (progresoVal) progresoVal.textContent = pctDia + '%';
            if (progresoBar) progresoBar.style.width = pctDia + '%';
            // Update the count badge too
            if (countEl) {
              countEl.textContent = checkedNow + '/' + allChecks.length + ' completados';
              countEl.style.background = (checkedNow === allChecks.length) ? 'rgba(16, 185, 129, 0.3)' : 'rgba(16, 185, 129, 0.2)';
            }
          }

          // Initial day progress from server data
          var initPct = ejList.length > 0 ? Math.round(((data.ejerciciosCompletados || 0) / ejList.length) * 100) : 0;
          if (progresoSection) progresoSection.classList.remove('d-none');
          if (progresoVal) progresoVal.textContent = initPct + '%';
          setTimeout(function () { if (progresoBar) progresoBar.style.width = initPct + '%'; }, 80);

          function updateCompletarButton() {
            if (!btnCompletar) return;
            var checks = lista.querySelectorAll('.ejercicio-check');
            var anyChecked = false;
            checks.forEach(function (c) { if (c.checked) anyChecked = true; });
            if (anyChecked) btnCompletar.classList.remove('d-none');
            else btnCompletar.classList.add('d-none');
          }

          // Completion button handler
          if (btnCompletar) {
            // Remove old listeners by replacing the button
            var newBtn = btnCompletar.cloneNode(true);
            btnCompletar.parentNode.replaceChild(newBtn, btnCompletar);
            btnCompletar = newBtn;
            if (!allCompleted) btnCompletar.classList.remove('d-none');

            btnCompletar.addEventListener('click', function () {
              var checks = lista.querySelectorAll('.ejercicio-check:checked');
              if (checks.length === 0) return;

              var ejerciciosPayload = [];
              checks.forEach(function (c) {
                ejerciciosPayload.push({
                  ejercicioId: parseInt(c.dataset.ejercicioId),
                  completado: true,
                  series: parseInt(c.dataset.series) || 0,
                  repeticiones: parseInt(c.dataset.repeticiones) || 0,
                  peso: parseFloat(c.dataset.peso) || 0
                });
              });

              var body = {
                rutinaAsignadaId: currentRutinaAsignadaId,
                fecha: currentFecha,
                ejercicios: ejerciciosPayload
              };

              btnCompletar.disabled = true;
              btnCompletar.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Guardando...';

              fetch('/usuario/api/calendario/completar-ejercicios', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
              })
                .then(function (r) { return r.json(); })
                .then(function (result) {
                  if (result.success) {
                    // Update day progress to reflect all currently checked boxes
                    updateDayProgress();
                    btnCompletar.innerHTML = '<i class="bi bi-check-circle-fill me-2"></i>Guardado';
                    btnCompletar.style.background = 'rgba(16, 185, 129, 0.3)';
                    // Refresh calendar events
                    calendar.refetchEvents();
                    // Close modal after a brief delay
                    setTimeout(function () {
                      if (modal) modal.hide();
                    }, 1200);
                  } else {
                    btnCompletar.disabled = false;
                    btnCompletar.innerHTML = '<i class="bi bi-check-circle-fill me-2"></i>Completar Seleccionados';
                    alert('Error: ' + (result.error || result.mensaje || 'No se pudo guardar'));
                  }
                })
                .catch(function () {
                  btnCompletar.disabled = false;
                  btnCompletar.innerHTML = '<i class="bi bi-check-circle-fill me-2"></i>Completar Seleccionados';
                  alert('Error de conexión al guardar');
                });
            });
          }
        })
        .catch(function () {
          if (myGen !== modalGeneration) return;
          if (loading) loading.classList.add('d-none');
          if (sinEjercicios) sinEjercicios.classList.remove('d-none');
        });
    }

    var calendar = new FullCalendar.Calendar(calendarEl, {
      initialView: 'dayGridMonth',
      locale: 'es',
      dayHeaderFormat: { weekday: 'short' },
      height: 'auto',
      aspectRatio: 1.55,
      firstDay: 1,
      dayMaxEventRows: 3,
      headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: ''
      },
      events: function (fetchInfo, successCallback, failureCallback) {
        var url = '/usuario/api/calendario/sesiones?start=' + fetchInfo.startStr.substring(0, 10) + '&end=' + fetchInfo.endStr.substring(0, 10);
        fetch(url)
          .then(function (r) { return r.json(); })
          .then(function (data) {
            if (Array.isArray(data)) successCallback(data);
            else failureCallback(data);
          })
          .catch(failureCallback);
      },
      eventClick: function (info) {
        info.jsEvent.preventDefault();
        showDayDetail(info);
      }
    });

    calendar.render();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initUsuarioCalendar);
  } else {
    initUsuarioCalendar();
  }
})();
