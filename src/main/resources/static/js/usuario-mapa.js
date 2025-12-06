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
  ,
  // Entradas añadidas desde petición del usuario
  {
    id: 9,
    nombre: 'Parque Simón Bolívar (Street Workout)',
    tipo: 'publico',
    lng: -74.086400,
    lat: 4.646550,
    direccion: 'Parque Simón Bolívar, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.2,
    servicios: ['Ejercicio al aire libre', 'Street Workout']
  },
  {
    id: 10,
    nombre: 'Parque El Tunal',
    tipo: 'publico',
    lng: -74.124300,
    lat: 4.568600,
    direccion: 'Parque El Tunal, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 11,
    nombre: 'Parque Timiza',
    tipo: 'publico',
    lng: -74.170900,
    lat: 4.612400,
    direccion: 'Parque Timiza, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 12,
    nombre: 'Parque San Andrés',
    tipo: 'publico',
    lng: -74.103000,
    lat: 4.707700,
    direccion: 'Parque San Andrés, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 13,
    nombre: 'Parque Servitá',
    tipo: 'publico',
    lng: -74.032200,
    lat: 4.757800,
    direccion: 'Parque Servitá, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 14,
    nombre: 'Parque Cayetano Cañizares',
    tipo: 'publico',
    lng: -74.168700,
    lat: 4.630900,
    direccion: 'Parque Cayetano Cañizares, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 15,
    nombre: 'Parque Primero de Mayo',
    tipo: 'publico',
    lng: -74.091700,
    lat: 4.586100,
    direccion: 'Parque Primero de Mayo, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 16,
    nombre: 'Smart Fit - Avenida Chile',
    tipo: 'privado',
    lng: -74.058370,
    lat: 4.664420,
    direccion: 'Smart Fit - Av. Chile, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 5:00 AM - 11:00 PM',
    rating: 4.1,
    servicios: ['Pesas', 'Cardio']
  },
  {
    id: 17,
    nombre: 'Smart Fit - 20 de Julio',
    tipo: 'privado',
    lng: -74.084800,
    lat: 4.586700,
    direccion: 'Smart Fit - 20 de Julio, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 5:00 AM - 11:00 PM',
    rating: 4.0,
    servicios: ['Pesas', 'Cardio']
  },
  {
    id: 18,
    nombre: 'Smart Fit - Calle 80',
    tipo: 'privado',
    lng: -74.048700,
    lat: 4.697400,
    direccion: 'Smart Fit - Calle 80, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 5:00 AM - 11:00 PM',
    rating: 4.0,
    servicios: ['Pesas', 'Cardio']
  },
  {
    id: 19,
    nombre: 'Bodytech Chapinero',
    tipo: 'privado',
    lng: -74.056090,
    lat: 4.646790,
    direccion: 'Bodytech Chapinero, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Sáb: 5:00 AM - 11:00 PM',
    rating: 4.2,
    servicios: ['Pesas', 'Clases']
  },
  {
    id: 20,
    nombre: 'Bodytech Calle 90',
    tipo: 'privado',
    lng: -74.049880,
    lat: 4.673010,
    direccion: 'Bodytech Calle 90, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Sáb: 5:00 AM - 11:00 PM',
    rating: 4.2,
    servicios: ['Pesas', 'Clases']
  },
  {
    id: 21,
    nombre: 'Bodytech Autopista 170',
    tipo: 'privado',
    lng: -74.041400,
    lat: 4.735100,
    direccion: 'Bodytech Autopista 170, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Sáb: 5:00 AM - 11:00 PM',
    rating: 4.1,
    servicios: ['Pesas', 'Clases']
  },
  {
    id: 22,
    nombre: 'Spinning Center Gym - Sede Admón.',
    tipo: 'privado',
    lng: -74.059290,
    lat: 4.654810,
    direccion: 'Spinning Center Gym, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Sáb: 5:00 AM - 11:00 PM',
    rating: 4.0,
    servicios: ['Spinning', 'Cardio']
  },
  {
    id: 23,
    nombre: 'Spinning Center Gym - Salitre',
    tipo: 'privado',
    lng: -74.122100,
    lat: 4.659900,
    direccion: 'Spinning Center Gym - Salitre, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Sáb: 5:00 AM - 11:00 PM',
    rating: 4.0,
    servicios: ['Spinning', 'Cardio']
  },
  {
    id: 24,
    nombre: 'Fitness24Seven Chapinero (24 hrs)',
    tipo: 'privado',
    lng: -74.062000,
    lat: 4.651700,
    direccion: 'Fitness24Seven Chapinero, Bogotá',
    telefono: 'N/A',
    horario: '24 horas',
    rating: 4.1,
    servicios: ['24 horas', 'Pesas', 'Cardio']
  }
  ,
  // Coordenadas adicionales solicitadas
  {
    id: 25,
    nombre: 'Parque de Calistenia Ferrocaja',
    tipo: 'publico',
    lng: -74.154000,
    lat: 4.634600,
    direccion: 'Parque de Calistenia Ferrocaja, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Calistenia', 'Ejercicio al aire libre']
  },
  {
    id: 26,
    nombre: 'Calistenia Parque El Virrey',
    tipo: 'publico',
    lng: -74.053100,
    lat: 4.686500,
    direccion: 'Parque El Virrey, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Calistenia', 'Ejercicio al aire libre']
  },
  {
    id: 27,
    nombre: 'Fitness Sports Park Eduardo Santos',
    tipo: 'publico',
    lng: -74.074500,
    lat: 4.594600,
    direccion: 'Fitness Sports Park Eduardo Santos, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 28,
    nombre: 'Parque Gustavo Uribe Botero',
    tipo: 'publico',
    lng: -74.101100,
    lat: 4.634500,
    direccion: 'Parque Gustavo Uribe Botero, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 29,
    nombre: 'Parque Portugal',
    tipo: 'publico',
    lng: -74.035500,
    lat: 4.706100,
    direccion: 'Parque Portugal, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 30,
    nombre: 'Ecoparque Sierra Morena',
    tipo: 'publico',
    lng: -74.158100,
    lat: 4.551100,
    direccion: 'Ecoparque Sierra Morena, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 6:00 PM',
    rating: 4.0,
    servicios: ['Senderismo', 'Ejercicio al aire libre']
  },
  {
    id: 31,
    nombre: 'Parque Taller El Ensueño',
    tipo: 'publico',
    lng: -74.181200,
    lat: 4.551200,
    direccion: 'Parque Taller El Ensueño, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 32,
    nombre: 'Parque Metropolitano El Recreo',
    tipo: 'publico',
    lng: -74.205900,
    lat: 4.646800,
    direccion: 'Parque Metropolitano El Recreo, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  }
  ,
  // Últimos puntos añadidos según petición
  {
    id: 33,
    nombre: 'Parque Nacional Enrique Olaya Herrera',
    tipo: 'publico',
    lng: -74.062600,
    lat: 4.619100,
    direccion: 'Parque Nacional Enrique Olaya Herrera, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.1,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 34,
    nombre: 'Parque Ciudad Berna',
    tipo: 'publico',
    lng: -74.075400,
    lat: 4.597600,
    direccion: 'Parque Ciudad Berna, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 35,
    nombre: 'Parque de Galerías',
    tipo: 'publico',
    lng: -74.075500,
    lat: 4.646800,
    direccion: 'Parque de Galerías, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 36,
    nombre: 'Parque Zonal Fontanar del Río',
    tipo: 'publico',
    lng: -74.086400,
    lat: 4.752300,
    direccion: 'Parque Zonal Fontanar del Río, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 37,
    nombre: 'Parque Lineal El Salitre (cerca a Biblioteca Virgilio Barco)',
    tipo: 'publico',
    lng: -74.089100,
    lat: 4.646500,
    direccion: 'Parque Lineal El Salitre, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 38,
    nombre: 'Parque Zonal La Granja',
    tipo: 'publico',
    lng: -74.124000,
    lat: 4.685200,
    direccion: 'Parque Zonal La Granja, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
  },
  {
    id: 39,
    nombre: 'Parque Urbanización San Andrés (Suba)',
    tipo: 'publico',
    lng: -74.082000,
    lat: 4.721500,
    direccion: 'Parque Urbanización San Andrés, Bogotá',
    telefono: 'N/A',
    horario: 'Lun-Dom: 6:00 AM - 8:00 PM',
    rating: 4.0,
    servicios: ['Ejercicio al aire libre']
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
