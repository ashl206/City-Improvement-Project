
import {LeafletMap, TileLayer, Marker, Circle, Polygon, Icon} from 'leaflet';

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


var treeIcon = new Icon({
    iconUrl: 'tree_icon.png',
    shadowUrl: 'tree_icon-shadow.png',

    iconSize:     [38, 95], // size of the icon
    shadowSize:   [50, 64], // size of the shadow
    iconAnchor:   [22, 94], // point of the icon which will correspond to marker's location
    shadowAnchor: [4, 62],  // the same for the shadow
    popupAnchor:  [-3, -76]

})

var treeMarker = new Marker([51.52, -0.10], {
    icon: treeIcon
}).addTo(map);
