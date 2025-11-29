// === FlowFit - Mapa de Gimnasios con Mapbox ===
// Obtén tu token gratuito en: https://account.mapbox.com/access-tokens/
mapboxgl.accessToken = 'pk.eyJ1IjoiZGFubzE3NzUiLCJhIjoiY21pang5eXZyMTljdTNmcHh0NXRpMTNvMiJ9.IxhzCrKNKC_pW6hkUMJosQ';

let map;
let markers = [];
let userMarker;
let currentPosition = { lng: -74.0629621, lat: 4.6518757 }; // Bogotá por defecto

// Datos de gimnasios (simulados - en producción vendrían del backend)
const gimnasios = [
  {
    id: 1,
    nombre: 'Bodytech Andino',
    tipo: 'privado',
    lng: -74.0540,
    lat: 4.6718,
    direccion: 'Cra. 11 #82-71, Bogotá',
    telefono: '+57 1 234 5678',
    horario: 'Lun-Vie: 5:00 AM - 11:00 PM',
    rating: 4.5,
    servicios: ['Pesas', 'Cardio', 'Clases Grupales', 'Spa', 'Piscina']
  },
  {
    id: 2,
    nombre: 'Smart Fit Colina',
    tipo: 'privado',
    lng: -74.0480,
    lat: 4.6850,
    direccion: 'Calle 127 #19-20, Bogotá',
    telefono: '+57 1 345 6789',
    horario: '24 horas',
    rating: 4.3,
    servicios: ['Pesas', 'Cardio', 'Clases', 'Ducha']
  },
  {
    id: 3,
    nombre: 'Gimnasio Público Simón Bolívar',
    tipo: 'publico',
    lng: -74.0920,
    lat: 4.6650,
    direccion: 'Parque Simón Bolívar, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 6:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre', 'Barras', 'Canchas']
  },
  {
    id: 4,
    nombre: 'Hard Body Gym Chapinero',
    tipo: 'privado',
    lng: -74.0630,
    lat: 4.6420,
    direccion: 'Calle 53 #13-30, Bogotá',
    telefono: '+57 1 456 7890',
    horario: 'Lun-Sáb: 6:00 AM - 10:00 PM',
    rating: 4.7,
    servicios: ['Pesas', 'CrossFit', 'Entrenamiento Personal']
  },
  {
    id: 5,
    nombre: 'Parque El Virrey - Zona Fitness',
    tipo: 'publico',
    lng: -74.0520,
    lat: 4.6580,
    direccion: 'Parque El Virrey, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 5:00 AM - 8:00 PM',
    rating: 4.2,
    servicios: ['Ejercicio al aire libre', 'Pista de trote', 'Máquinas']
  },
  {
    id: 6,
    nombre: 'Fitness One Salitre',
    tipo: 'privado',
    lng: -74.0820,
    lat: 4.6480,
    direccion: 'Av. El Dorado #69-76, Bogotá',
    telefono: '+57 1 567 8901',
    horario: 'Lun-Vie: 5:30 AM - 10:00 PM',
    rating: 4.4,
    servicios: ['Pesas', 'Cardio', 'Spinning', 'Yoga']
  },
  {
    id: 7,
    nombre: 'Gimnasio Público Parkway',
    tipo: 'publico',
    lng: -74.0680,
    lat: 4.6720,
    direccion: 'Calle 100 con Autopista Norte, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 7:00 PM',
    rating: 3.8,
    servicios: ['Barras', 'Flexiones', 'Abdominales']
  },
  {
    id: 8,
    nombre: 'Olympic Gym Usaquén',
    tipo: 'privado',
    lng: -74.0300,
    lat: 4.6950,
    direccion: 'Calle 120 #7-45, Bogotá',
    telefono: '+57 1 678 9012',
    horario: 'Lun-Sáb: 6:00 AM - 11:00 PM',
    rating: 4.6,
    servicios: ['Pesas', 'Cardio', 'Funcional', 'Nutrición']
  }
];

// Inicializar mapa
function iniciarMap() {
  console.log('Inicializando mapa Mapbox...');
  
  // Ocultar loading
  const loadingEl = document.getElementById('mapLoading');
  if (loadingEl) {
    loadingEl.style.display = 'none';
  }

  // Crear mapa con Mapbox
  map = new mapboxgl.Map({
    container: 'map',
    style: 'mapbox://styles/mapbox/dark-v11', // Estilo oscuro profesional
    center: [currentPosition.lng, currentPosition.lat],
    zoom: 13,
    pitch: 0
  });

  // Agregar controles de navegación
  map.addControl(new mapboxgl.NavigationControl(), 'top-right');
  map.addControl(new mapboxgl.FullscreenControl(), 'top-right');

  // Cargar gimnasios cuando el mapa esté listo
  map.on('load', function() {
    cargarGimnasios();
  });

  // Event listeners
  document.getElementById('btnMiUbicacion').addEventListener('click', centrarEnMiUbicacion);
  document.getElementById('btnAplicarFiltros').addEventListener('click', aplicarFiltros);

  // Intentar obtener ubicación del usuario
  obtenerUbicacionUsuario();
}

// Obtener ubicación del usuario
function obtenerUbicacionUsuario() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        currentPosition = {
          lng: position.coords.longitude,
          lat: position.coords.latitude
        };
        
        // Actualizar marcador de usuario
        if (userMarker) {
          userMarker.remove();
        }
        
        // Crear marcador de usuario con estilo personalizado
        const el = document.createElement('div');
        el.className = 'user-marker';
        el.style.width = '20px';
        el.style.height = '20px';
        el.style.borderRadius = '50%';
        el.style.backgroundColor = '#4F46E5';
        el.style.border = '3px solid white';
        el.style.boxShadow = '0 0 10px rgba(79, 70, 229, 0.5)';

        userMarker = new mapboxgl.Marker(el)
          .setLngLat([currentPosition.lng, currentPosition.lat])
          .addTo(map);

        map.flyTo({
          center: [currentPosition.lng, currentPosition.lat],
          zoom: 14
        });

        // Recargar gimnasios por distancia
        cargarGimnasios();
      },
      (error) => {
        console.error('Error obteniendo ubicación:', error);
      }
    );
  }
}

// Centrar en mi ubicación
function centrarEnMiUbicacion() {
  const btn = document.getElementById('btnMiUbicacion');
  btn.disabled = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Ubicando...';

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        currentPosition = {
          lng: position.coords.longitude,
          lat: position.coords.latitude
        };
        
        map.flyTo({
          center: [currentPosition.lng, currentPosition.lat],
          zoom: 15,
          duration: 1500
        });

        if (userMarker) {
          userMarker.setLngLat([currentPosition.lng, currentPosition.lat]);
        }

        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-crosshair me-2"></i>Mi Ubicación';
      },
      (error) => {
        alert('No se pudo obtener tu ubicación');
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-crosshair me-2"></i>Mi Ubicación';
      }
    );
  }
}

// Cargar gimnasios en el mapa y lista
function cargarGimnasios() {
  // Limpiar marcadores existentes
  markers.forEach(m => m.marker.remove());
  markers = [];

  // Obtener filtros
  const mostrarPublicos = document.getElementById('filtroPublico').checked;
  const mostrarPrivados = document.getElementById('filtroPrivado').checked;

  // Filtrar gimnasios
  const gimnasiosFiltrados = gimnasios.filter(gym => {
    if (gym.tipo === 'publico' && !mostrarPublicos) return false;
    if (gym.tipo === 'privado' && !mostrarPrivados) return false;
    return true;
  });

  // Calcular distancias y ordenar
  const gimnasiosConDistancia = gimnasiosFiltrados.map(gym => ({
    ...gym,
    distancia: calcularDistancia(currentPosition.lat, currentPosition.lng, gym.lat, gym.lng)
  })).sort((a, b) => a.distancia - b.distancia);

  // Crear marcadores
  gimnasiosConDistancia.forEach(gym => {
    // Crear elemento del marcador
    const el = document.createElement('div');
    el.className = 'custom-marker';
    el.innerHTML = `
      <div style="
        width: 36px;
        height: 36px;
        background: ${gym.tipo === 'publico' ? 'linear-gradient(135deg, #10b981, #059669)' : 'linear-gradient(135deg, #f59e0b, #d97706)'};
        border-radius: 50% 50% 50% 0;
        transform: rotate(-45deg);
        border: 3px solid white;
        box-shadow: 0 4px 8px rgba(0,0,0,0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
      ">
        <i class="bi ${gym.tipo === 'publico' ? 'bi-tree' : 'bi-building'}" style="
          transform: rotate(45deg);
          color: white;
          font-size: 16px;
        "></i>
      </div>
    `;

    // Crear popup
    const popup = new mapboxgl.Popup({ offset: 25, closeButton: false })
      .setHTML(`
        <div class="custom-info-window">
          <h6>${gym.nombre}</h6>
          <p><i class="bi bi-geo-alt me-2"></i>${gym.direccion}</p>
          <p><i class="bi bi-clock me-2"></i>${gym.horario}</p>
          ${gym.telefono !== 'N/A' ? `<p><i class="bi bi-telephone me-2"></i>${gym.telefono}</p>` : ''}
          <p><strong>Servicios:</strong> ${gym.servicios.join(', ')}</p>
          <button class="btn-directions" onclick="abrirDirecciones(${gym.lat}, ${gym.lng})">
            <i class="bi bi-arrow-up-right-circle me-2"></i>Cómo llegar
          </button>
        </div>
      `);

    // Crear marcador
    const marker = new mapboxgl.Marker(el)
      .setLngLat([gym.lng, gym.lat])
      .setPopup(popup)
      .addTo(map);

    // Event listener para click
    el.addEventListener('click', () => {
      seleccionarGimnasio(gym.id);
    });

    markers.push({ marker, gym });
  });

  // Actualizar lista
  actualizarListaGimnasios(gimnasiosConDistancia);

  // Actualizar contador
  document.getElementById('contadorGimnasios').textContent = gimnasiosConDistancia.length;
}

// Actualizar lista de gimnasios
function actualizarListaGimnasios(gimnasios) {
  const lista = document.getElementById('listaGimnasios');
  
  if (gimnasios.length === 0) {
    lista.innerHTML = `
      <div class="empty-state">
        <i class="bi bi-inbox"></i>
        <p>No se encontraron gimnasios con los filtros seleccionados</p>
      </div>
    `;
    return;
  }

  lista.innerHTML = gimnasios.map(gym => `
    <div class="gym-card" data-gym-id="${gym.id}" onclick="seleccionarGimnasio(${gym.id})">
      <div class="d-flex gap-3">
        <div class="gym-icon ${gym.tipo}">
          <i class="bi ${gym.tipo === 'publico' ? 'bi-tree' : 'bi-building'}"></i>
        </div>
        <div class="flex-grow-1">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <div>
              <div class="gym-name">${gym.nombre}</div>
              <span class="gym-type ${gym.tipo}">${gym.tipo === 'publico' ? 'Público' : 'Privado'}</span>
            </div>
            <div class="gym-rating">
              <i class="bi bi-star-fill"></i>
              <span class="text-white">${gym.rating}</span>
            </div>
          </div>
          <div class="gym-info">
            <div class="gym-info-item">
              <i class="bi bi-geo-alt-fill"></i>
              <span>${gym.distancia.toFixed(1)} km de distancia</span>
            </div>
            <div class="gym-info-item">
              <i class="bi bi-clock"></i>
              <span>${gym.horario}</span>
            </div>
            ${gym.telefono !== 'N/A' ? `
            <div class="gym-info-item">
              <i class="bi bi-telephone"></i>
              <span>${gym.telefono}</span>
            </div>
            ` : ''}
          </div>
        </div>
      </div>
    </div>
  `).join('');
}

// Seleccionar gimnasio
function seleccionarGimnasio(gymId) {
  // Remover clase active de todas las cards
  document.querySelectorAll('.gym-card').forEach(card => card.classList.remove('active'));
  
  // Agregar clase active a la card seleccionada
  const card = document.querySelector(`[data-gym-id="${gymId}"]`);
  if (card) card.classList.add('active');

  // Encontrar gimnasio y marcador
  const markerData = markers.find(m => m.gym.id === gymId);
  if (markerData) {
    map.flyTo({
      center: [markerData.gym.lng, markerData.gym.lat],
      zoom: 16,
      duration: 1000
    });
    
    // Abrir popup
    markerData.marker.togglePopup();
  }
}

// Abrir direcciones en Google Maps
function abrirDirecciones(lat, lng) {
  const url = `https://www.google.com/maps/dir/?api=1&destination=${lat},${lng}`;
  window.open(url, '_blank');
}

// Aplicar filtros
function aplicarFiltros() {
  const modal = bootstrap.Modal.getInstance(document.getElementById('modalFiltros'));
  modal.hide();
  cargarGimnasios();
}

// Calcular distancia entre dos puntos (fórmula Haversine)
function calcularDistancia(lat1, lon1, lat2, lon2) {
  const R = 6371; // Radio de la Tierra en km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

// Exportar funciones al scope global
window.iniciarMap = iniciarMap;
window.seleccionarGimnasio = seleccionarGimnasio;
window.abrirDirecciones = abrirDirecciones;
