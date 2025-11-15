// JS para inicializar Google Maps en la vista de usuario
function iniciarMap(){
    // Coordenadas por defecto (ejemplo: Bogotá)
    var coord = {lat: 4.6518757, lng: -74.0629621};

    // Crear el mapa con opciones básicas
    var map = new google.maps.Map(document.getElementById('map'),{
      zoom: 12,
      center: coord,
      mapTypeControl: false,
      streetViewControl: false,
      fullscreenControl: true
    });

    // Marcadores de ejemplo (puedes reemplazarlos por datos reales desde el servidor)
    var puntos = [
      {pos: {lat: 4.6518757, lng: -74.0629621}, title: 'Punto de inicio', desc: 'Punto de referencia'},
      {pos: {lat: 4.6700000, lng: -74.0500000}, title: 'Gimnasio A', desc: 'Gimnasio cercano'},
      {pos: {lat: 4.6400000, lng: -74.0800000}, title: 'Parque B', desc: 'Área para correr'}
    ];

    var infoWindow = new google.maps.InfoWindow();

    puntos.forEach(function(p){
      var m = new google.maps.Marker({ position: p.pos, map: map, title: p.title });
      m.addListener('click', function(){
        infoWindow.setContent('<div class="map-info-window"><strong>' + p.title + '</strong><div>' + p.desc + '</div></div>');
        infoWindow.open(map, m);
      });
    });

    // Añadir control personalizado: botón 'Mi ubicación'
    addLocateControl(map);
}

// Control que centra el mapa en la ubicación del usuario
function addLocateControl(map){
  var controlDiv = document.createElement('div');
  controlDiv.style.margin = '8px';

  var controlUI = document.createElement('button');
  controlUI.className = 'map-locate-btn';
  controlUI.title = 'Centrar en mi ubicación';
  controlUI.innerHTML = '<i class="bi bi-geo-alt-fill"></i>';
  controlDiv.appendChild(controlUI);

  controlUI.addEventListener('click', function(){
    if(navigator.geolocation){
      controlUI.disabled = true;
      controlUI.innerHTML = '<span class="spinner-border spinner-border-sm text-light" role="status" aria-hidden="true"></span>';
      navigator.geolocation.getCurrentPosition(function(position){
        var pos = { lat: position.coords.latitude, lng: position.coords.longitude };
        map.setCenter(pos);
        map.setZoom(14);
        new google.maps.Marker({ position: pos, map: map, title: 'Mi ubicación' });
        controlUI.disabled = false;
        controlUI.innerHTML = '<i class="bi bi-geo-alt-fill"></i>';
      }, function(error){
        alert('No se pudo obtener la ubicación: ' + error.message);
        controlUI.disabled = false;
        controlUI.innerHTML = '<i class="bi bi-geo-alt-fill"></i>';
      }, { enableHighAccuracy: true, timeout: 10000 });
    } else {
      alert('Geolocalización no está disponible en este navegador.');
    }
  });

  map.controls[google.maps.ControlPosition.RIGHT_TOP].push(controlDiv);
}

// Exportar la función al scope global (asegura que el callback la encuentre)
window.iniciarMap = iniciarMap;