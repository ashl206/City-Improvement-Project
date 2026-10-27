import {LeafletMap, TileLayer, Marker, Circle, Polygon} from 'leaflet';

var map = new LeafletMap('map').setView([51.505, -0.09], 13);

new TileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
}).addTo(map);

var marker = new Marker([51.5, -0.09]).addTo(map);

var circle = new Circle([51.508, -0.11], {
    color: 'red',
    fillColor: '#f03',
    fillOpacity: 0.5,
    radius: 500
}).addTo(map);

var polygon = new Polygon([
    [51.509, -0.08],
    [51.503, -0.06],
    [51.51, -0.047]
]).addTo(map);
