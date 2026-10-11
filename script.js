    import {LeafletMap, TileLayer, Marker, Circle, Polygon, Popup} from 'leaflet';
    const map = new LeafletMap('map').setView([51.505, -0.09] ,13);

    new TileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);

const marker = new Marker([51.5, -0.09]).addTo(map);


