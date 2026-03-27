(function () {
  function toDateOnlyStr(dateObj) {
    if (!dateObj) return null;
    var d = new Date(dateObj.getTime());
    d.setHours(0, 0, 0, 0);
    // YYYY-MM-DD
    var y = d.getFullYear();
    var m = String(d.getMonth() + 1).padStart(2, '0');
    var day = String(d.getDate()).padStart(2, '0');
    return y + '-' + m + '-' + day;
  }

  function postForm(url, params) {
    var body = new URLSearchParams();
    Object.keys(params || {}).forEach(function (k) {
      var v = params[k];
      if (v === undefined || v === null) return;
      if (Array.isArray(v)) {
        v.forEach(function (item) {
          if (item !== undefined && item !== null) body.append(k, String(item));
        });
      } else {
        body.set(k, String(v));
      }
    });

    return fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: body.toString()
    }).then(function (r) {
      return r.json().then(function (data) {
        if (!r.ok) {
          var msg = (data && data.error) ? data.error : 'Error'
          throw new Error(msg);
        }
        return data;
      });
    });
  }

  function initPlanificador() {
    var calendarEl = document.getElementById('calendarEntrenadorPlanificador');
    if (!calendarEl || typeof FullCalendar === 'undefined') return;

    var asignacionId = window.FF_ASIGNACION_ID;
    var validStart = window.FF_VALID_START;

    var btnGuardar = document.getElementById('btnGuardarPlanificacion');
    var badgeEstado = document.querySelector('.badge.rounded-pill');
    var saveOk = document.getElementById('ffSaveOk');
    var saveWarn = document.getElementById('ffSaveWarn');
    var saveWarnText = document.getElementById('ffSaveWarnText');
    function showSaveWarn(text) {
      if (saveWarn && saveWarnText) {
        if (text) {
          saveWarnText.textContent = text;
          saveWarn.classList.remove('d-none');
        } else {
          saveWarnText.textContent = '';
          saveWarn.classList.add('d-none');
        }
      }
      if (saveOk) saveOk.classList.add('d-none');
    }
    function showSaveOk() {
      showSaveWarn(null);
      if (saveOk) saveOk.classList.remove('d-none');
    }

    function apiGuardarPlanUrl() {
      return '/entrenador/asignaciones/' + encodeURIComponent(asignacionId) + '/guardar-planificacion';
    }

    function estadoActual() {
      try {
        return (badgeEstado && badgeEstado.textContent ? badgeEstado.textContent : '').trim().toUpperCase();
      } catch (e) {
        return '';
      }
    }

    if (btnGuardar) {
      btnGuardar.addEventListener('click', function () {
        showSaveWarn(null);
        var ok = confirm(estadoActual() === 'BORRADOR'
          ? '¿Guardar planificación y asignar la rutina al usuario?'
          : '¿Guardar cambios de la planificación?');
        if (!ok) return;

        btnGuardar.disabled = true;
        postForm(apiGuardarPlanUrl(), {})
          .then(function () {
            showSaveOk();
            btnGuardar.innerHTML = '<i class="bi bi-check2 me-2"></i>Guardado';
            btnGuardar.disabled = false;
          })
          .catch(function (err) {
            btnGuardar.disabled = false;
            showSaveWarn(err && err.message ? err.message : 'Error al guardar');
          });
      });
    }

    var dndStatusEl = document.getElementById('ffDndStatus');
    var dndStatusTextEl = document.getElementById('ffDndStatusText');
    function showDndWarn(text) {
      if (!dndStatusEl || !dndStatusTextEl) return;
      if (text) {
        dndStatusTextEl.textContent = text;
        dndStatusEl.classList.remove('d-none');
      } else {
        dndStatusTextEl.textContent = '';
        dndStatusEl.classList.add('d-none');
      }
    }

    var draggablesEl = document.getElementById('ejerciciosDraggables');
    var diasDraggablesEl = document.getElementById('diasDraggables');
    var DraggableCtor = FullCalendar.Draggable
      || (FullCalendar.Interaction && FullCalendar.Interaction.Draggable)
      || (FullCalendar.interaction && FullCalendar.interaction.Draggable);

    if (draggablesEl && DraggableCtor) {
      new DraggableCtor(draggablesEl, {
        itemSelector: '.ff-draggable-ejercicio',
        eventData: function (eventEl) {
          var titulo = eventEl.getAttribute('data-title') || eventEl.textContent || 'Ejercicio';
          var ejercicioId = eventEl.getAttribute('data-ejercicio-id');
          return {
            title: String(titulo).trim(),
            allDay: true,
            extendedProps: {
              ejercicioId: ejercicioId ? parseInt(ejercicioId, 10) : null
            }
          };
        }
      });
      showDndWarn(null);
    } else if (draggablesEl) {
      // Si esto aparece, casi seguro falta el plugin @fullcalendar/interaction
      showDndWarn('Drag & drop no disponible: no se cargó el plugin de interacción del calendario.');
    }

    if (diasDraggablesEl && DraggableCtor) {
      new DraggableCtor(diasDraggablesEl, {
        itemSelector: '.ff-draggable-dia',
        eventData: function (eventEl) {
          var titulo = eventEl.getAttribute('data-title') || eventEl.textContent || 'Día';
          var diaOrden = eventEl.getAttribute('data-dia-orden');
          return {
            title: String(titulo).trim(),
            allDay: true,
            extendedProps: {
              diaOrden: diaOrden ? parseInt(diaOrden, 10) : null
            }
          };
        }
      });
      showDndWarn(null);
    }

    var modalEl = document.getElementById('modalDetalleDia');
    var modal = null;
    if (modalEl && window.bootstrap) {
      modal = new bootstrap.Modal(modalEl);
    }

    var fechaLabelEl = modalEl ? modalEl.querySelector('[data-ff-fecha]') : null;
    var listEl = document.getElementById('listaEjerciciosDia');
    var warnBox = document.getElementById('detalleDiaWarn');
    var warnText = document.getElementById('detalleDiaWarnText');
    var currentFecha = null;

    function showWarn(text) {
      if (!warnBox || !warnText) return;
      if (text) {
        warnText.textContent = text;
        warnBox.classList.remove('d-none');
      } else {
        warnText.textContent = '';
        warnBox.classList.add('d-none');
      }
    }

    function apiDetalleDiaUrl(fechaStr) {
      return '/entrenador/api/asignaciones/' + encodeURIComponent(asignacionId)
        + '/ejercicios-programados/dia?fecha=' + encodeURIComponent(fechaStr);
    }

    function apiReordenarUrl() {
      return '/entrenador/api/asignaciones/' + encodeURIComponent(asignacionId)
        + '/ejercicios-programados/reordenar';
    }

    function apiEliminarUrl(programadoId) {
      return '/entrenador/api/ejercicios-programados/' + encodeURIComponent(programadoId) + '/eliminar';
    }

    function cargarDetalleDia(fechaStr) {
      if (!listEl) return Promise.resolve([]);
      showWarn(null);
      listEl.innerHTML = '';

      return fetch(apiDetalleDiaUrl(fechaStr))
        .then(function (r) {
          return r.json().then(function (data) {
            if (!r.ok) {
              var msg = (data && data.error) ? data.error : 'Error al cargar';
              throw new Error(msg);
            }
            return data;
          });
        })
        .then(function (items) {
          if (!Array.isArray(items) || items.length === 0) {
            var empty = document.createElement('div');
            empty.className = 'text-muted-flowfit small';
            empty.textContent = 'No hay ejercicios programados para este día.';
            listEl.appendChild(empty);
            return [];
          }

          items.forEach(function (it) {
            var row = document.createElement('div');
            row.className = 'list-group-item d-flex align-items-center justify-content-between bg-transparent text-white border rounded-3 mb-2';
            row.setAttribute('draggable', 'true');
            row.setAttribute('data-programado-id', String(it.id));

            var left = document.createElement('div');
            left.className = 'd-flex align-items-center gap-2';

            var handle = document.createElement('span');
            handle.className = 'text-muted-flowfit';
            handle.innerHTML = '<i class="bi bi-grip-vertical"></i>';

            var name = document.createElement('div');
            name.className = 'fw-semibold';
            name.textContent = it.nombre || 'Ejercicio';

            left.appendChild(handle);
            left.appendChild(name);

            var right = document.createElement('div');
            right.className = 'd-flex align-items-center gap-2';

            var del = document.createElement('button');
            del.type = 'button';
            del.className = 'btn btn-sm btn-entrenador-outline';
            del.innerHTML = '<i class="bi bi-trash"></i>';
            del.addEventListener('click', function () {
              var ok = confirm('¿Eliminar este ejercicio del día?');
              if (!ok) return;
              postForm(apiEliminarUrl(it.id), {})
                .then(function () {
                  return cargarDetalleDia(currentFecha);
                })
                .then(function () {
                  calendar.refetchEvents();
                })
                .catch(function (err) {
                  showWarn(err && err.message ? err.message : 'Error al eliminar');
                });
            });

            right.appendChild(del);

            row.appendChild(left);
            row.appendChild(right);

            listEl.appendChild(row);
          });

          enableListDnd();
          return items;
        })
        .catch(function (err) {
          showWarn(err && err.message ? err.message : 'Error al cargar');
          return [];
        });
    }

    function getListOrderIds() {
      if (!listEl) return [];
      var els = Array.prototype.slice.call(listEl.querySelectorAll('[data-programado-id]'));
      return els.map(function (el) { return parseInt(el.getAttribute('data-programado-id'), 10); })
        .filter(function (n) { return !isNaN(n); });
    }

    var dragSrcEl = null;

    function enableListDnd() {
      if (!listEl) return;
      var items = Array.prototype.slice.call(listEl.querySelectorAll('[data-programado-id]'));

      items.forEach(function (item) {
        item.addEventListener('dragstart', function (e) {
          dragSrcEl = item;
          e.dataTransfer.effectAllowed = 'move';
          e.dataTransfer.setData('text/plain', item.getAttribute('data-programado-id'));
        });

        item.addEventListener('dragover', function (e) {
          e.preventDefault();
          e.dataTransfer.dropEffect = 'move';
        });

        item.addEventListener('drop', function (e) {
          e.preventDefault();
          if (!dragSrcEl || dragSrcEl === item) return;

          var rect = item.getBoundingClientRect();
          var before = (e.clientY - rect.top) < (rect.height / 2);
          if (before) {
            listEl.insertBefore(dragSrcEl, item);
          } else {
            listEl.insertBefore(dragSrcEl, item.nextSibling);
          }

          var ids = getListOrderIds();
          postForm(apiReordenarUrl(), { fecha: currentFecha, ids: ids })
            .catch(function (err) {
              showWarn(err && err.message ? err.message : 'Error al reordenar');
            });
        });
      });
    }

    function openDetalleDia(fechaStr) {
      currentFecha = fechaStr;
      if (fechaLabelEl) fechaLabelEl.textContent = fechaStr;
      cargarDetalleDia(fechaStr);
      if (modal) modal.show();
    }

    function listUrl(fetchInfo) {
      var startStr = fetchInfo && fetchInfo.start ? toDateOnlyStr(fetchInfo.start) : fetchInfo.startStr;
      var endStr = fetchInfo && fetchInfo.end ? toDateOnlyStr(fetchInfo.end) : fetchInfo.endStr;
      return '/entrenador/api/asignaciones/' + encodeURIComponent(asignacionId)
        + '/ejercicios-programados?start=' + encodeURIComponent(startStr)
        + '&end=' + encodeURIComponent(endStr);
    }

    var calendar = new FullCalendar.Calendar(calendarEl, {
      initialView: 'dayGridMonth',
      locale: 'es',
      dayHeaderFormat: { weekday: 'short' },
      height: 'auto',
      aspectRatio: 1.55,
      firstDay: 1,
      initialDate: validStart || undefined,
      // Sin límites: el entrenador puede planificar en cualquier fecha
      headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: ''
      },
      themeSystem: 'bootstrap5',
      editable: true,
      droppable: true,
      eventDurationEditable: false,
      eventStartEditable: false,
      eventOverlap: true,
      dayMaxEventRows: 2,
      events: function (fetchInfo, successCallback, failureCallback) {
        fetch(listUrl(fetchInfo))
          .then(function (r) { return r.json(); })
          .then(function (data) {
            if (Array.isArray(data)) successCallback(data);
            else failureCallback(data);
          })
          .catch(failureCallback);
      },
      eventReceive: function (info) {
        // Create on backend
        var ejercicioId = info.event.extendedProps && info.event.extendedProps.ejercicioId;
        var diaOrden = info.event.extendedProps && info.event.extendedProps.diaOrden;
        var fecha = info.event.start ? toDateOnlyStr(info.event.start) : info.event.startStr;

        if ((!ejercicioId && !diaOrden) || !fecha) {
          info.event.remove();
          return;
        }

        var url = null;
        var payload = { fecha: fecha };

        if (diaOrden) {
          url = '/entrenador/api/asignaciones/' + encodeURIComponent(asignacionId)
            + '/ejercicios-programados/programar-dia';
          payload.diaOrden = diaOrden;
        } else {
          url = '/entrenador/api/asignaciones/' + encodeURIComponent(asignacionId) + '/ejercicios-programados';
          payload.ejercicioId = ejercicioId;
        }

        postForm(url, payload)
          .then(function () {
            info.event.remove();
            calendar.refetchEvents();
          })
          .catch(function (err) {
            alert(err && err.message ? err.message : 'Error al programar');
            info.event.remove();
          });
      },
      eventClick: function (info) {
        info.jsEvent.preventDefault();
        var fecha = (info.event.extendedProps && info.event.extendedProps.fecha)
          ? info.event.extendedProps.fecha
          : (info.event.start ? toDateOnlyStr(info.event.start) : info.event.startStr);
        if (!fecha) return;
        openDetalleDia(fecha);
      }
    });

    calendar.render();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initPlanificador);
  } else {
    initPlanificador();
  }
})();
