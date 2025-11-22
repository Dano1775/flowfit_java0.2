/**
 * ===== FLOWFIT DASHBOARD - SHARED JAVASCRIPT =====
 * Scripts compartidos para dashboards de Entrenador y Usuario
 */

// ===== SIDEBAR COLAPSABLE =====
document.addEventListener('DOMContentLoaded', function() {
    // Elementos del DOM
    const sidebar = document.querySelector('.sidebar-base');
    const mainContent = document.querySelector('.main-content-base');
    const collapseBtn = document.getElementById('sidebarCollapseBtn');
    
    // Recuperar estado del sidebar del localStorage
    const sidebarCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
    
    // Aplicar estado guardado al cargar
    if (sidebarCollapsed && sidebar && mainContent && collapseBtn) {
        sidebar.classList.add('collapsed');
        mainContent.classList.add('sidebar-collapsed');
        collapseBtn.classList.add('collapsed');
        
        const icon = collapseBtn.querySelector('i');
        if (icon) {
            icon.classList.remove('bi-chevron-left');
            icon.classList.add('bi-chevron-right');
        }
    }
    
    // Toggle sidebar al hacer clic
    if (collapseBtn) {
        collapseBtn.addEventListener('click', function() {
            if (sidebar && mainContent) {
                // Toggle clases
                sidebar.classList.toggle('collapsed');
                mainContent.classList.toggle('sidebar-collapsed');
                collapseBtn.classList.toggle('collapsed');
                
                // Cambiar icono
                const icon = collapseBtn.querySelector('i');
                if (icon) {
                    if (sidebar.classList.contains('collapsed')) {
                        icon.classList.remove('bi-chevron-left');
                        icon.classList.add('bi-chevron-right');
                        localStorage.setItem('sidebarCollapsed', 'true');
                    } else {
                        icon.classList.remove('bi-chevron-right');
                        icon.classList.add('bi-chevron-left');
                        localStorage.setItem('sidebarCollapsed', 'false');
                    }
                }
            }
        });
    }
    
    // Cerrar sidebar en móvil al hacer clic fuera
    if (window.innerWidth < 768) {
        document.addEventListener('click', function(e) {
            if (sidebar && !sidebar.contains(e.target) && !collapseBtn.contains(e.target)) {
                sidebar.classList.remove('show');
            }
        });
    }
});

// ===== ANIMATED COUNTERS =====
function animateCounter(element, start, end, duration) {
    if (!element) return;
    
    const range = end - start;
    const increment = range / (duration / 16); // 60fps
    let current = start;
    
    const timer = setInterval(function() {
        current += increment;
        
        if ((increment > 0 && current >= end) || (increment < 0 && current <= end)) {
            current = end;
            clearInterval(timer);
        }
        
        // Formatear número según el tipo
        if (element.dataset.type === 'percentage') {
            element.textContent = Math.round(current) + '%';
        } else if (element.dataset.type === 'currency') {
            element.textContent = '$' + Math.round(current).toLocaleString();
        } else if (element.dataset.type === 'decimal') {
            element.textContent = current.toFixed(1);
        } else {
            element.textContent = Math.round(current);
        }
    }, 16);
}

// Inicializar contadores cuando sean visibles
document.addEventListener('DOMContentLoaded', function() {
    const counters = document.querySelectorAll('.counter');
    
    if (counters.length > 0) {
        // IntersectionObserver para animar cuando entren en vista
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting && !entry.target.classList.contains('animated')) {
                    const target = parseInt(entry.target.dataset.target) || 0;
                    animateCounter(entry.target, 0, target, 2000);
                    entry.target.classList.add('animated');
                }
            });
        }, { threshold: 0.5 });
        
        counters.forEach(counter => observer.observe(counter));
    }
});

// ===== RELOJ EN TIEMPO REAL =====
function updateTime() {
    const timeElement = document.getElementById('currentTime');
    const dateElement = document.getElementById('currentDate');
    
    if (timeElement || dateElement) {
        const now = new Date();
        
        if (timeElement) {
            const hours = String(now.getHours()).padStart(2, '0');
            const minutes = String(now.getMinutes()).padStart(2, '0');
            timeElement.textContent = `${hours}:${minutes}`;
        }
        
        if (dateElement) {
            const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
            dateElement.textContent = now.toLocaleDateString('es-ES', options);
        }
    }
}

// Actualizar cada segundo
document.addEventListener('DOMContentLoaded', function() {
    updateTime();
    setInterval(updateTime, 1000);
});

// ===== ACTIVE NAVIGATION =====
document.addEventListener('DOMContentLoaded', function() {
    const navLinks = document.querySelectorAll('.nav-link-base');
    const currentPath = window.location.pathname;
    
    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (href && currentPath.includes(href)) {
            link.classList.add('active');
        }
    });
});

// ===== TOOLTIPS BOOTSTRAP =====
document.addEventListener('DOMContentLoaded', function() {
    // Inicializar tooltips de Bootstrap si están disponibles
    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
});

// ===== POPOVERS BOOTSTRAP =====
document.addEventListener('DOMContentLoaded', function() {
    // Inicializar popovers de Bootstrap si están disponibles
    if (typeof bootstrap !== 'undefined' && bootstrap.Popover) {
        const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
        popoverTriggerList.map(function (popoverTriggerEl) {
            return new bootstrap.Popover(popoverTriggerEl);
        });
    }
});

// ===== SMOOTH SCROLL =====
document.addEventListener('DOMContentLoaded', function() {
    const links = document.querySelectorAll('a[href^="#"]');
    
    links.forEach(link => {
        link.addEventListener('click', function(e) {
            const targetId = this.getAttribute('href');
            if (targetId && targetId !== '#') {
                const targetElement = document.querySelector(targetId);
                if (targetElement) {
                    e.preventDefault();
                    targetElement.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            }
        });
    });
});

// ===== FADE IN ANIMATIONS =====
document.addEventListener('DOMContentLoaded', function() {
    const fadeElements = document.querySelectorAll('.fade-in-up');
    
    if (fadeElements.length > 0) {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry, index) => {
                if (entry.isIntersecting) {
                    setTimeout(() => {
                        entry.target.style.opacity = '1';
                        entry.target.style.transform = 'translateY(0)';
                    }, index * 100); // Stagger animation
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.1 });
        
        fadeElements.forEach(element => {
            element.style.opacity = '0';
            element.style.transform = 'translateY(30px)';
            element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            observer.observe(element);
        });
    }
});

// ===== CARD HOVER EFFECTS =====
document.addEventListener('DOMContentLoaded', function() {
    const cards = document.querySelectorAll('.stat-card-enhanced, .card-usuario, .card-entrenador');
    
    cards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-8px)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
});

// ===== SEARCH FUNCTIONALITY =====
function filterTable(input, tableId) {
    const filter = input.value.toUpperCase();
    const table = document.getElementById(tableId);
    
    if (!table) return;
    
    const rows = table.getElementsByTagName('tr');
    
    for (let i = 1; i < rows.length; i++) { // Skip header row
        const row = rows[i];
        const cells = row.getElementsByTagName('td');
        let found = false;
        
        for (let j = 0; j < cells.length; j++) {
            const cell = cells[j];
            if (cell) {
                const textValue = cell.textContent || cell.innerText;
                if (textValue.toUpperCase().indexOf(filter) > -1) {
                    found = true;
                    break;
                }
            }
        }
        
        row.style.display = found ? '' : 'none';
    }
}

// ===== LOADING STATES =====
function showLoading(buttonElement) {
    if (!buttonElement) return;
    
    buttonElement.disabled = true;
    buttonElement.dataset.originalHtml = buttonElement.innerHTML;
    buttonElement.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Cargando...';
}

function hideLoading(buttonElement) {
    if (!buttonElement) return;
    
    buttonElement.disabled = false;
    if (buttonElement.dataset.originalHtml) {
        buttonElement.innerHTML = buttonElement.dataset.originalHtml;
    }
}

// ===== TOAST NOTIFICATIONS =====
function showToast(message, type = 'info') {
    // Crear el toast si no existe el contenedor
    let toastContainer = document.getElementById('toastContainer');
    
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toastContainer';
        toastContainer.className = 'position-fixed top-0 end-0 p-3';
        toastContainer.style.zIndex = '9999';
        document.body.appendChild(toastContainer);
    }
    
    const toastId = 'toast-' + Date.now();
    const bgClass = type === 'success' ? 'bg-success' : type === 'error' ? 'bg-danger' : 'bg-info';
    
    const toastHtml = `
        <div id="${toastId}" class="toast align-items-center text-white ${bgClass} border-0" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;
    
    toastContainer.insertAdjacentHTML('beforeend', toastHtml);
    
    const toastElement = document.getElementById(toastId);
    if (toastElement && typeof bootstrap !== 'undefined' && bootstrap.Toast) {
        const toast = new bootstrap.Toast(toastElement, { autohide: true, delay: 3000 });
        toast.show();
        
        // Eliminar del DOM después de ocultarse
        toastElement.addEventListener('hidden.bs.toast', function() {
            toastElement.remove();
        });
    }
}

// ===== CONFIRM DIALOGS =====
function confirmAction(message, callback) {
    if (confirm(message)) {
        callback();
    }
}

// ===== KEYBOARD SHORTCUTS =====
document.addEventListener('keydown', function(e) {
    // Ctrl + B para toggle sidebar
    if (e.ctrlKey && e.key === 'b') {
        e.preventDefault();
        const collapseBtn = document.getElementById('sidebarCollapseBtn');
        if (collapseBtn) {
            collapseBtn.click();
        }
    }
    
    // Escape para cerrar modales
    if (e.key === 'Escape') {
        const modals = document.querySelectorAll('.modal.show');
        modals.forEach(modal => {
            const bsModal = bootstrap.Modal.getInstance(modal);
            if (bsModal) {
                bsModal.hide();
            }
        });
    }
});

// ===== FORM VALIDATION =====
document.addEventListener('DOMContentLoaded', function() {
    const forms = document.querySelectorAll('.needs-validation');
    
    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });
});

// ===== AUTO LOGOUT WARNING =====
let inactivityTimer;
const INACTIVITY_TIMEOUT = 30 * 60 * 1000; // 30 minutos

function resetInactivityTimer() {
    clearTimeout(inactivityTimer);
    inactivityTimer = setTimeout(() => {
        showToast('Tu sesión está por expirar por inactividad', 'warning');
        setTimeout(() => {
            window.location.href = '/logout';
        }, 60000); // 1 minuto después de la advertencia
    }, INACTIVITY_TIMEOUT);
}

// Eventos que resetean el timer
['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart'].forEach(event => {
    document.addEventListener(event, resetInactivityTimer, true);
});

// Iniciar el timer
document.addEventListener('DOMContentLoaded', resetInactivityTimer);

// ===== EXPORT FUNCTIONS =====
window.FlowFitDashboard = {
    showLoading,
    hideLoading,
    showToast,
    confirmAction,
    filterTable,
    animateCounter
};
