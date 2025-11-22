// Manejo simple del menú de perfil para FlowFit
(function () {
  const trigger = document.getElementById('flowfitProfileTrigger');
  const menu = document.getElementById('flowfitProfileMenu');

  if (!trigger || !menu) return;

  function toggleMenu(e) {
    e.stopPropagation();
    const active = menu.classList.contains('active');
    if (active) {
      menu.classList.remove('active');
      trigger.setAttribute('aria-expanded', 'false');
    } else {
      menu.classList.add('active');
      trigger.setAttribute('aria-expanded', 'true');
    }
  }

  function closeMenu() {
    if (menu.classList.contains('active')) {
      menu.classList.remove('active');
      trigger.setAttribute('aria-expanded', 'false');
    }
  }

  trigger.addEventListener('click', toggleMenu);
  // Cerrar al hacer click fuera
  document.addEventListener('click', function (ev) {
    const isInside = trigger.contains(ev.target) || menu.contains(ev.target);
    if (!isInside) closeMenu();
  });
  // Cerrar con Escape
  document.addEventListener('keydown', function (ev) {
    if (ev.key === 'Escape') closeMenu();
  });
})();
