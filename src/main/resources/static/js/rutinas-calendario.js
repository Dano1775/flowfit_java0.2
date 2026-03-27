(function () {
  function initUsuarioCalendar() {
    var calendarEl = document.getElementById('calendarUsuario');
    if (!calendarEl || typeof FullCalendar === 'undefined') return;

    var modalEl = document.getElementById('modalEventoRutina');
    var modal = null;
    if (modalEl && window.bootstrap) {
      modal = new bootstrap.Modal(modalEl);
    }

    function setModalContent(info) {
      if (!modalEl) return;
      var titleEl = modalEl.querySelector('[data-ff-title]');
      var dateEl = modalEl.querySelector('[data-ff-date]');
      var stateEl = modalEl.querySelector('[data-ff-state]');
      var startBtn = modalEl.querySelector('[data-ff-start]');

      var eventObj = info.event;
      var startStr = eventObj.startStr;
      var rutinaAsignadaId = eventObj.extendedProps && eventObj.extendedProps.rutinaAsignadaId;
      var estado = eventObj.extendedProps && eventObj.extendedProps.estado;
      var esDescanso = eventObj.extendedProps && eventObj.extendedProps.esDescanso;
      var programada = eventObj.extendedProps && eventObj.extendedProps.programada;

      if (titleEl) titleEl.textContent = eventObj.title || 'Rutina';
      if (dateEl) dateEl.textContent = startStr;
      if (stateEl) {
        if (programada === false) stateEl.textContent = 'SIN PROGRAMAR';
        else stateEl.textContent = esDescanso ? 'DESCANSO' : (estado || '');
      }

      if (startBtn) {
        if (rutinaAsignadaId && !esDescanso && programada !== false) {
          startBtn.href = '/usuario/sesion/' + rutinaAsignadaId + '?fecha=' + encodeURIComponent(startStr);
          startBtn.classList.remove('disabled');
        } else {
          startBtn.href = '#';
          startBtn.classList.add('disabled');
        }
      }
    }

    var calendar = new FullCalendar.Calendar(calendarEl, {
      initialView: 'dayGridMonth',
      locale: 'es',
      height: 'auto',
      firstDay: 1,
      headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: ''
      },
      events: function (fetchInfo, successCallback, failureCallback) {
        var url = '/usuario/api/calendario/sesiones?start=' + fetchInfo.startStr + '&end=' + fetchInfo.endStr;
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
        setModalContent(info);
        if (modal) modal.show();
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
