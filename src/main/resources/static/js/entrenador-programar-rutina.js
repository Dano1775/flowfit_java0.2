(function () {
  function initProgramacion() {
    var calendarEl = document.getElementById('calendarEntrenadorProgramacion');
    if (!calendarEl || typeof FullCalendar === 'undefined') return;

    var asignacionId = window.FF_ASIGNACION_ID;
    var dias = Array.isArray(window.FF_DIAS_OPTIONS) ? window.FF_DIAS_OPTIONS : [];
    var validStart = window.FF_VALID_START;
    var validEndExcl = window.FF_VALID_END_EXCL;

    var modalEl = document.getElementById('modalProgramarDia');
    var modal = null;
    if (modalEl && window.bootstrap) {
      modal = new bootstrap.Modal(modalEl);
    }

    var fechaEl = modalEl ? modalEl.querySelector('[data-ff-fecha]') : null;
    var estadoEl = modalEl ? modalEl.querySelector('[data-ff-estado]') : null;
    var selectEl = document.getElementById('selectDiaPlantilla');
    var btnGuardar = document.getElementById('btnGuardarProgramacion');

    var warnBox = document.getElementById('programarWarn');
    var warnText = document.getElementById('programarWarnText');

    var currentFecha = null;
    var currentEstado = null;

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

    function populateSelect() {
      if (!selectEl) return;
      // Keep first option (Sin programar)
      while (selectEl.options.length > 1) selectEl.remove(1);

      dias.forEach(function (d) {
        var opt = document.createElement('option');
        opt.value = d.id;
        var nombre = (d.nombre && String(d.nombre).trim()) ? d.nombre : ('Día ' + d.orden);
        var tipo = d.tipo ? String(d.tipo) : '';
        opt.textContent = tipo === 'DESCANSO' ? ('Descanso — ' + nombre) : nombre;
        selectEl.appendChild(opt);
      });
    }

    populateSelect();

    function openModalForDate(dateStr, extendedProps) {
      currentFecha = dateStr;
      currentEstado = extendedProps && extendedProps.estado ? extendedProps.estado : null;

      showWarn(null);

      if (fechaEl) fechaEl.textContent = dateStr;
      if (estadoEl) estadoEl.textContent = currentEstado || 'PROGRAMADA';

      var diaId = extendedProps && extendedProps.diaId != null ? String(extendedProps.diaId) : '';
      if (selectEl) {
        selectEl.value = diaId;
      }

      // Prevent editing realized/canceled sessions
      var disabled = currentEstado === 'REALIZADA' || currentEstado === 'CANCELADA';
      if (selectEl) selectEl.disabled = disabled;
      if (btnGuardar) btnGuardar.disabled = disabled;
      if (disabled) {
        showWarn('Esta sesión está ' + currentEstado + ' y no se puede reprogramar.');
      }

      if (modal) modal.show();
    }

    function postAssignDay(fechaStr, rutinaDiaId) {
      var url = '/entrenador/api/asignaciones/' + encodeURIComponent(asignacionId) + '/sesiones/asignar-dia';

      var body = new URLSearchParams();
      body.set('fecha', fechaStr);
      if (rutinaDiaId) body.set('rutinaDiaId', rutinaDiaId);

      return fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body.toString()
      }).then(function (r) {
        return r.json().then(function (data) {
          if (!r.ok) {
            var msg = (data && data.error) ? data.error : 'Error al guardar';
            throw new Error(msg);
          }
          return data;
        });
      });
    }

    if (btnGuardar) {
      btnGuardar.addEventListener('click', function () {
        if (!currentFecha) return;

        showWarn(null);

        var diaId = selectEl ? selectEl.value : '';
        var diaIdToSend = diaId && String(diaId).trim() ? String(diaId).trim() : null;

        btnGuardar.disabled = true;

        postAssignDay(currentFecha, diaIdToSend)
          .then(function () {
            if (modal) modal.hide();
            calendar.refetchEvents();
          })
          .catch(function (err) {
            showWarn(err && err.message ? err.message : 'Error al guardar');
          })
          .finally(function () {
            btnGuardar.disabled = false;
          });
      });
    }

    var calendar = new FullCalendar.Calendar(calendarEl, {
      initialView: 'dayGridMonth',
      locale: 'es',
      height: 'auto',
      firstDay: 1,
      initialDate: validStart || undefined,
      validRange: (validStart && validEndExcl) ? { start: validStart, end: validEndExcl } : undefined,
      headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: ''
      },
      events: function (fetchInfo, successCallback, failureCallback) {
        var url = '/entrenador/api/asignaciones/' + encodeURIComponent(asignacionId)
          + '/sesiones?start=' + fetchInfo.startStr + '&end=' + fetchInfo.endStr;

        fetch(url)
          .then(function (r) { return r.json(); })
          .then(function (data) {
            if (Array.isArray(data)) successCallback(data);
            else failureCallback(data);
          })
          .catch(failureCallback);
      },
      dateClick: function (info) {
        if (validStart && validEndExcl) {
          if (info.dateStr < validStart || info.dateStr >= validEndExcl) return;
        }
        openModalForDate(info.dateStr, { estado: 'PROGRAMADA', diaId: '' });
      },
      eventClick: function (info) {
        info.jsEvent.preventDefault();
        openModalForDate(info.event.startStr, info.event.extendedProps);
      }
    });

    calendar.render();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initProgramacion);
  } else {
    initProgramacion();
  }
})();
