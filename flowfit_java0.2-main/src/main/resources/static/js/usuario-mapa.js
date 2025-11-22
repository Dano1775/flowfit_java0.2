function iniciarMap() {
    var coord = { lat: 4.6518757, lng: -74.0629621 };

    var map = new google.maps.Map(document.getElementById('map'), {
        zoom: 12,
        center: coord,
        mapTypeControl: false,
        streetViewControl: false,
        fullscreenControl: true,
        // No styles aquí para que el mapa sea claro
    });

    var infoWindow = new google.maps.InfoWindow();

    // Lista de marcadores dinámicos (parques y ubicación del usuario)
    var markers = [];

    // Marcadores de ejemplo
    var puntos = [
        { pos: { lat: 4.6518757, lng: -74.0629621 }, title: 'Punto de inicio', desc: 'Punto de referencia' },
        { pos: { lat: 4.6700000, lng: -74.0500000 }, title: 'Gimnasio A', desc: 'Gimnasio cercano' },
        { pos: { lat: 4.6480496, lng: -74.0717832 }, title: 'Gimnasio A', desc: 'Gimnasio cercano' }
    ];

    puntos.forEach(function(p) {
        var m = new google.maps.Marker({ position: p.pos, map: map, title: p.title });
        m.addListener('click', function() {
            infoWindow.setContent('<div class="map-info-window"><strong>' + p.title + '</strong><div>' + p.desc + '</div></div>');
            infoWindow.open(map, m);
        });
        markers.push(m);
    });

    function buscarParques(location) {
        if (!google.maps.places) {
            console.error('La librería Places no está cargada. Añade "libraries=places" al cargar la API de Google Maps.');
            return;
        }

        var service = new google.maps.places.PlacesService(map);

        // limpiar marcadores anteriores añadidos por buscarParques
        function clearPlaceMarkers() {
            markers.forEach(function(m) { m.setMap(null); });
            markers = [];
        }

        clearPlaceMarkers();

        service.nearbySearch({
            location: location,
            radius: 50000, // 50 km
            type: 'park'
        }, function(results, status, pagination) {
            console.log('Places nearbySearch status:', status, 'results count:', results && results.length);
            if (status === google.maps.places.PlacesServiceStatus.OK && results && results.length) {
                var bounds = new google.maps.LatLngBounds();

                results.forEach(function(place) {
                    if (!place.geometry || !place.geometry.location) return;
                    var marker = new google.maps.Marker({
                        map: map,
                        position: place.geometry.location,
                        title: place.name,
                        icon: { url: 'https://maps.google.com/mapfiles/ms/icons/green-dot.png' }
                    });

                    marker.addListener('click', function() {
                        infoWindow.setContent('<div class="map-info-window"><strong>' + place.name + '</strong><div>' + (place.vicinity || '') + '</div></div>');
                        infoWindow.open(map, marker);
                    });

                    markers.push(marker);
                    bounds.extend(place.geometry.location);
                });

                if (!bounds.isEmpty()) {
                    map.fitBounds(bounds);
                    if (results.length === 1) map.setZoom(Math.min(map.getZoom(), 15));
                }

                if (pagination && pagination.hasNextPage) pagination.nextPage();
            } else if (status === google.maps.places.PlacesServiceStatus.ZERO_RESULTS) {
                console.info('No se encontraron parques cerca de la ubicación.');
            } else {
                console.warn('Error en Places nearbySearch:', status);
            }
        });
    }

    buscarParques(coord);

    addLocateControl(map, buscarParques);
}

function addLocateControl(map, buscarParques) {
    var controlDiv = document.createElement('div');
    controlDiv.style.margin = '8px';

    var controlUI = document.createElement('button');
    controlUI.className = 'map-locate-btn';
    controlUI.title = 'Centrar en mi ubicación';
    controlUI.innerHTML = '<i class="bi bi-geo-alt-fill"></i>';
    controlDiv.appendChild(controlUI);

    controlUI.addEventListener('click', function() {
        if (navigator.geolocation) {
            controlUI.disabled = true;
            controlUI.innerHTML = '<span class="spinner-border spinner-border-sm text-light" role="status" aria-hidden="true"></span>';
            navigator.geolocation.getCurrentPosition(function(position) {
                var pos = { lat: position.coords.latitude, lng: position.coords.longitude };
                map.setCenter(pos);
                map.setZoom(14);
                new google.maps.Marker({ position: pos, map: map, title: 'Mi ubicación' });

                buscarParques(pos);

                controlUI.disabled = false;
                controlUI.innerHTML = '<i class="bi bi-geo-alt-fill"></i>';
            }, function(error) {
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

window.iniciarMap = iniciarMap;
