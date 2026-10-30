
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


// Shadow anchor and shadow size not necessary for shadowUrl being null 
var treeIcon = new Icon({
    iconUrl: 'tree_icon.png',
    shadowUrl: null,

    iconSize:     [88, 95], // size of the icon
    // shadowSize:   [50, 64], // size of the shadow
    iconAnchor:   [22, 94], // point of the icon which will correspond to marker's location
    // shadowAnchor: [4, 62],  // the same for the shadow
    popupAnchor:  [-3, -76]

})

var treeMarker = new Marker([51.52, -0.10], {
    icon: treeIcon
}).addTo(map);

var flowerIcon = new Icon({
    iconUrl: 'flower_icon.jpg',
    shadowUrl: null,

    iconSize: [78, 95],
    iconAnchor: [22, 94],
    popupAnchor: [-3, -76]
})

var flowerMarker = new Marker([51.515, -0.08], {
    icon: flowerIcon
}).addTo(map);

var raisedMedianIcon = new Icon({
    iconUrl: 'median_icon.webp',
    shadowUrl: null, 

    iconSize: [88, 95],
    iconAnchor: [22, 94],
    popupAnchor: [-3, -76]
})

var raisedMedianIconMarker = new Marker([51.525, -0.11], {
    icon: raisedMedianIcon
}).addTo(map);
