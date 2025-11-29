// === FlowFit - Mapa de Gimnasios ===
let map;
let markers = [];
let infoWindow;
let userMarker;
let currentPosition = { lat: 4.6518757, lng: -74.0629621 }; // Bogotá por defecto

// Datos de gimnasios (simulados - en producción vendrían del backend)
const gimnasos = [
  {
    id: 1,
    nombre: 'Bodytech Andino',
    tipo: 'privado',
    lat: 4.6718,
    lng: -74.0540,
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
    lat: 4.6850,
    lng: -74.0480,
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
    lat: 4.6650,
    lng: -74.0920,
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
    lat: 4.6420,
    lng: -74.0630,
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
    lat: 4.6580,
    lng: -74.0520,
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
    lat: 4.6480,
    lng: -74.0820,
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
    lat: 4.6720,
    lng: -74.0680,
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
    lat: 4.6950,
    lng: -74.0300,
    direccion: 'Calle 120 #7-45, Bogotá',
    telefono: '+57 1 678 9012',
    horario: 'Lun-Sáb: 6:00 AM - 11:00 PM',
    rating: 4.6,
    servicios: ['Pesas', 'Cardio', 'Funcional', 'Nutrición']
  }
];

// Inicializar mapa
function iniciarMap() {
  // Ocultar loading
  document.getElementById('mapLoading').style.display = 'none';

  // Crear mapa
  map = new google.maps.Map(document.getElementById('map'), {
    zoom: 13,
    center: currentPosition,
    styles: getMapStyles(),
    mapTypeControl: false,
    streetViewControl: false,
    fullscreenControl: true,
    zoomControl: true,
    zoomControlOptions: {
      position: google.maps.ControlPosition.RIGHT_CENTER
    }
  });

  infoWindow = new google.maps.InfoWindow();

  // Cargar gimnasios
  cargarGimnasios();

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
          lat: position.coords.latitude,
          lng: position.coords.longitude
        };
        
        // Actualizar marcador de usuario
        if (userMarker) {
          userMarker.setMap(null);
        }
        
        userMarker = new google.maps.Marker({
          position: currentPosition,
          map: map,
          title: 'Mi ubicación',
          icon: {
            path: google.maps.SymbolPath.CIRCLE,
            scale: 10,
            fillColor: '#4F46E5',
            fillOpacity: 1,
            strokeColor: '#ffffff',
            strokeWeight: 3
          },
          zIndex: 1000
        });

        map.setCenter(currentPosition);
        map.setZoom(14);

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
          lat: position.coords.latitude,
          lng: position.coords.longitude
        };
        
        map.setCenter(currentPosition);
        map.setZoom(15);

        if (userMarker) {
          userMarker.setPosition(currentPosition);
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
  markers.forEach(m => m.marker.setMap(null));
  markers = [];

  // Obtener filtros
  const mostrarPublicos = document.getElementById('filtroPublico').checked;
  const mostrarPrivados = document.getElementById('filtroPrivado').checked;

  // Filtrar gimnasios
  const gimnasiosFiltrados = gimnasos.filter(gym => {
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
    const marker = new google.maps.Marker({
      position: { lat: gym.lat, lng: gym.lng },
      map: map,
      title: gym.nombre,
      icon: {
        url: gym.tipo === 'publico' 
          ? 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(getMarkerSVG('#10b981'))
          : 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(getMarkerSVG('#f59e0b')),
        scaledSize: new google.maps.Size(40, 40)
      }
    });

    marker.addListener('click', () => mostrarInfoGimnasio(gym, marker));
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
    map.setCenter(markerData.marker.getPosition());
    map.setZoom(16);
    mostrarInfoGimnasio(markerData.gym, markerData.marker);
  }
}

// Mostrar información del gimnasio
function mostrarInfoGimnasio(gym, marker) {
  const content = `
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
  `;
  
  infoWindow.setContent(content);
  infoWindow.open(map, marker);
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

// SVG para marcador personalizado
function getMarkerSVG(color) {
  return `<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 40 40">
    <path fill="${color}" d="M20 0C12.8 0 7 5.8 7 13c0 10.5 13 27 13 27s13-16.5 13-27c0-7.2-5.8-13-13-13zm0 18c-2.8 0-5-2.2-5-5s2.2-5 5-5 5 2.2 5 5-2.2 5-5 5z"/>
    <circle cx="20" cy="13" r="3" fill="white"/>
  </svg>`;
}

// Estilos del mapa (modo oscuro)
function getMapStyles() {
  return [
    { elementType: 'geometry', stylers: [{ color: '#212121' }] },
    { elementType: 'labels.icon', stylers: [{ visibility: 'off' }] },
    { elementType: 'labels.text.fill', stylers: [{ color: '#757575' }] },
    { elementType: 'labels.text.stroke', stylers: [{ color: '#212121' }] },
    { featureType: 'administrative', elementType: 'geometry', stylers: [{ color: '#757575' }] },
    { featureType: 'poi', elementType: 'labels.text.fill', stylers: [{ color: '#757575' }] },
    { featureType: 'poi.park', elementType: 'geometry', stylers: [{ color: '#181818' }] },
    { featureType: 'poi.park', elementType: 'labels.text.fill', stylers: [{ color: '#616161' }] },
    { featureType: 'road', elementType: 'geometry.fill', stylers: [{ color: '#2c2c2c' }] },
    { featureType: 'road', elementType: 'labels.text.fill', stylers: [{ color: '#8a8a8a' }] },
    { featureType: 'road.arterial', elementType: 'geometry', stylers: [{ color: '#373737' }] },
    { featureType: 'road.highway', elementType: 'geometry', stylers: [{ color: '#3c3c3c' }] },
    { featureType: 'water', elementType: 'geometry', stylers: [{ color: '#000000' }] },
    { featureType: 'water', elementType: 'labels.text.fill', stylers: [{ color: '#3d3d3d' }] }
  ];
}

// Exportar función al scope global
window.iniciarMap = iniciarMap;
window.seleccionarGimnasio = seleccionarGimnasio;
window.abrirDirecciones = abrirDirecciones;