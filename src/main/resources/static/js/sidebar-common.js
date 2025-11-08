// Sidebar common functionality for admin and usuario
document.addEventListener('DOMContentLoaded', function() {
  const sidebar = document.getElementById('sidebar');
  const sidebarToggle = document.getElementById('sidebarToggle');
  const sidebarOverlay = document.getElementById('sidebarOverlay');
  const sidebarCollapseBtn = document.getElementById('sidebarCollapseBtn');

  // Mobile Toggle
  if (sidebarToggle) {
    sidebarToggle.addEventListener('click', () => {
      sidebar.classList.toggle('show');
      if (sidebarOverlay) sidebarOverlay.classList.toggle('show');
    });
  }

  // Overlay Click
  if (sidebarOverlay) {
    sidebarOverlay.addEventListener('click', () => {
      sidebar.classList.remove('show');
      sidebarOverlay.classList.remove('show');
    });
  }

  // Desktop Collapse
  if (sidebarCollapseBtn) {
    sidebarCollapseBtn.addEventListener('click', () => {
      sidebar.classList.toggle('collapsed');
      const icon = sidebarCollapseBtn.querySelector('i');
      if (icon) {
        icon.classList.toggle('bi-chevron-left');
        icon.classList.toggle('bi-chevron-right');
      }
    });
  }

  // Highlight active nav item based on current URL
  const currentPath = window.location.pathname;
  const navLinks = document.querySelectorAll('.nav-link-base, .nav-link-usuario');
  navLinks.forEach(link => {
    link.classList.remove('active');
    const href = link.getAttribute('href');
    if (href === currentPath) {
      link.classList.add('active');
    }
  });
});
