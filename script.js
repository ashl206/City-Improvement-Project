// leaflet import info and setup
import {LeafletMap, TileLayer, Marker, Circle, Polygon, Icon, Polyline} from 'leaflet';


// shared map setup for the analysis page!!!
function createMap(containerId, center, zoom) {
    const container = document.getElementById(containerId);

    // statement that just checks if the container exists before creating the map
    if (!container) return null;
    const map = new LeafletMap(containerId).setView(center, zoom);

        // openstreetmap tile layer
        new TileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(map);
    
    return map;
}

// HOME MAP SETUP
// - basically the original code just changed var to const

    /* im assuming that the homepage map will be like a demo map for display? maybe change the location to UCF coords? the analysis page maps will be the changing ones!! this one is like just for show, so its unchanging. currently it has the london coords */

const homeMap = createMap('map', [51.505, -0.09], 13);
    if (homeMap) {

        // icons for maps!!! - changed 'map' to 'homeMap'
        const marker = new Marker([51.5, -0.09]).addTo(homeMap);
    
        const circle = new Circle([51.508, -0.11], {
            color: 'red',
            fillColor: '#f03',
            fillOpacity: 0.5,
            radius: 500
        }).addTo(homeMap);

        const polygon = new Polygon([
            [51.509, -0.08],
            [51.503, -0.06],
            [51.51, -0.047]
        ]).addTo(homeMap);


        // Shadow anchor and shadow size not necessary for shadowUrl being null 
        const treeIcon = new Icon({
            iconUrl: 'tree_icon.png',
            shadowUrl: null,

            iconSize:     [88, 95], // size of the icon
            // shadowSize:   [50, 64], // size of the shadow
            iconAnchor:   [22, 94], // point of the icon which will correspond to marker's location
            // shadowAnchor: [4, 62],  // the same for the shadow
            popupAnchor:  [-3, -76]

        })

        const treeMarker = new Marker([51.52, -0.10], {
            icon: treeIcon
        }).addTo(homeMap);

        const flowerIcon = new Icon({
            iconUrl: 'flower_icon.jpg',
            shadowUrl: null,

            iconSize: [78, 95],
            iconAnchor: [22, 94],
            popupAnchor: [-3, -76]
        })

        const flowerMarker = new Marker([51.515, -0.08], {
            icon: flowerIcon
        }).addTo(homeMap);

        const raisedMedianIcon = new Icon({
            iconUrl: 'median_icon.webp',
            shadowUrl: null, 

            iconSize: [88, 95],
            iconAnchor: [22, 94],
            popupAnchor: [-3, -76]
        })

        const raisedMedianIconMarker = new Marker([51.525, -0.11], {
            icon: raisedMedianIcon
        }).addTo(homeMap);
    }




// ANALYSIS PAGE MAP SETUP

        // sample route to make sure that this works lol; currently just using london coords
        const sampleRoute = [ [51.505, -0.09], [51.508, -0.11], [51.510, -0.12] ];


// main analysis map - displays ONLY the route!!! no icons or anything else unless we wanna change
const analysisMap = createMap('analysis-map', [51.505, -0.09], 13);
    // shows the route line
    if (analysisMap) {
        new Polyline(sampleRoute, {
            color: 'blue',
            weight: 4,
            opacity: 0.8
        }).addTo(analysisMap);

        // shows the start and end markers
        new Marker(sampleRoute[0]).addTo(analysisMap);
        new Marker(sampleRoute[sampleRoute.length - 1]).addTo(analysisMap);

// before comparison map - displays the original route and issues 
const beforeMap = createMap('before-map', [51.505, -0.09], 13);


    if (beforeMap) {
        // the route line
        new Polyline(sampleRoute, {
            color: 'red',
            weight: 4,
            opacity: 0.8
        }).addTo(beforeMap);

        // circle icon to show issue location
        new Circle([51.508, -0.11], {
            color: 'red',
            fillColor: 'red',
            fillOpacity: 0.5,
            radius: 50
        }).addTo(beforeMap);
        
        // shows the start and end markers
        new Marker(sampleRoute[0]).addTo(beforeMap);
        new Marker(sampleRoute[sampleRoute.length - 1]).addTo(beforeMap);
    }

// before comparison map - displays the original route and issues 
const afterMap = createMap('after-map', [51.505, -0.09], 13);


    if (afterMap) {
        // the route line
        new Polyline(sampleRoute, {
            color: 'green',
            weight: 4,
            opacity: 0.8
        }).addTo(afterMap);

        // tree icon
        const treeIcon = new Icon({
            iconUrl: 'tree_icon.png',
            shadowUrl: null,
            iconSize: [88, 95],
            iconAnchor: [22, 94],
            popupAnchor: [-3, -76]
        });
            const treeMarker = new Marker([51.508, -0.095], {
                icon: treeIcon
            }).addTo(afterMap);

        //  flower icon
        const flowerIcon = new Icon({
            iconUrl: 'flower_icon.jpg',
            shadowUrl: null,
            iconSize: [78, 95],
            iconAnchor: [22, 94],
            popupAnchor: [-3, -76]
        });

            const flowerMarker = new Marker([51.515, -0.08], {
                icon: flowerIcon
            }).addTo(afterMap);

        // raised median icon
        const raisedMedianIcon = new Icon({
            iconUrl: 'median_icon.webp',
            shadowUrl: null,
            iconSize: [88, 95],
            iconAnchor: [22, 94],
            popupAnchor: [-3, -76]
        });

            const raisedMedianIconMarker = new Marker([51.525, -0.11], {
                icon: raisedMedianIcon
            }).addTo(afterMap);
                
        // shows the start and end markers
        new Marker(sampleRoute[0]).addTo(afterMap);
        new Marker(sampleRoute[sampleRoute.length - 1]).addTo(afterMap);
    }
    

}




