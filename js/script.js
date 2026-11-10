// // leaflet import info and setup
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



// Calling OpenStreetMaps API 
async function getCoordinates(address) {
    if (!address || !address.trim()) {
        throw new Error("Please enter an address.");
    }

    const url = new URL('https://nominatim.openstreetmap.org/search');

    url.search = new URLSearchParams({
        q: address,
        format: 'jsonv2',
        limit: '1'
    });

    const response = await fetch(url);
    if (!response.ok) {
        throw new Error("Failed to fetch coordinates. Please try again.");
    }


    const data = await response.json();
    if (data.length === 0) {
        throw new Error("Address not found.");
    }

    return [
        parseFloat(data[0].lat),
        parseFloat(data[0].lon)
    ];
}

// Matching coordinates retrieved from previous function to the address entered by the user
async function addAddressMarker(map, address) {
    if (!map) return;

    try {
        const coordinates = await getCoordinates(address);

        new Marker(coordinates).addTo(map);

        map.setView(coordinates, 15);

    } catch (error) {
        console.error(error.message);
    }
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


// main analysis map - displays ONLY the route!!! no icons or anything else unless we wanna change
const analysisMap = createMap('analysis-map', [28.60, -81.20], 13);

// before comparison map - displays the original route and issues 
const beforeMap = createMap('before-map', [28.60, -81.20], 13);

// before comparison map - displays the original route and issues 
const afterMap = createMap('after-map', [28.60, -81.20], 13);


// store the layers so we can remove old routes when the user searches for a new one.
const mapLayers = {
    analysis: [],
    before: [],
    after: []
};

// removes the previous route from a map
function clearMap(map, layers) {
    if (!map) return;

    layers.forEach(layer => {
        map.removeLayer(layer);
    });

    layers.length = 0;
}


// adds a layer to a map and saves it
function addMapLayer(map, layers, layer) {
    if (!map) return;

    layer.addTo(map);
    layers.push(layer);
}

// function used by analysis.js to display the searched route
export async function displayAddressRoute(origin, destination) {

    // convert starting address into coordinates
    const startCoordinates = await getCoordinates(origin);

    // wait before making the second request
    await new Promise(resolve => setTimeout(resolve, 1100));

    // convert destination into coordinates
    const endCoordinates = await getCoordinates(destination);


    // the two coordinate points make our temporary route
    const routeCoordinates = [
        startCoordinates,
        endCoordinates
    ];


    // find a midpoint for the temporary issue marker
    const midpoint = [
        (startCoordinates[0] + endCoordinates[0]) / 2,
        (startCoordinates[1] + endCoordinates[1]) / 2
    ];


    // get positions along the route for the improvement icons
    function getPointAlongRoute(position) {
        return [
            startCoordinates[0] +
            (endCoordinates[0] - startCoordinates[0]) * position,

            startCoordinates[1] +
            (endCoordinates[1] - startCoordinates[1]) * position
        ];
    }


    // clear previous search results
    clearMap(analysisMap, mapLayers.analysis);
    clearMap(beforeMap, mapLayers.before);
    clearMap(afterMap, mapLayers.after);





    // main analysis map - displays ONLY the route!!! no icons or anything else unless we wanna change

    // shows the route line
    if (analysisMap) {
        new Polyline(routeCoordinates, {
            color: 'blue',
            weight: 4,
            opacity: 0.8
        }).addTo(analysisMap);

        // shows the start and end markers
        new Marker(startCoordinates).addTo(analysisMap);
        new Marker(endCoordinates).addTo(analysisMap);
    }

    // before comparison map - displays the original route and issues
    if (beforeMap) {
        // the route line
        new Polyline(routeCoordinates, {
            color: 'red',
            weight: 4,
            opacity: 0.8
        }).addTo(beforeMap);

        // circle icon to show issue location
        new Circle(midpoint, {
            color: 'red',
            fillColor: 'red',
            fillOpacity: 0.5,
            radius: 50
        }).addTo(beforeMap);
        
        // shows the start and end markers
        new Marker(startCoordinates).addTo(beforeMap);
        new Marker(endCoordinates).addTo(beforeMap);
    }

    // after comparison map - displays the original route and issues 
    if (afterMap) {
        // the route line
        new Polyline(routeCoordinates, {
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
            const treeMarker = new Marker(getPointAlongRoute(0.3), {
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

            const flowerMarker = new Marker(getPointAlongRoute(0.5), {
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

            const raisedMedianIconMarker = new Marker(getPointAlongRoute(0.7), {
                icon: raisedMedianIcon
            }).addTo(afterMap);
                
        // shows the start and end markers
        new Marker(startCoordinates).addTo(afterMap);
        new Marker(endCoordinates).addTo(afterMap);
    }
    

    // zoom all maps to fit the route
    const maps = [analysisMap, beforeMap, afterMap];
    maps.forEach(map => {
        if (map) {
            map.fitBounds(routeCoordinates, {
                padding: [30, 30]
            });
        }
    });

    // return coordinates 
    return {
        origin: startCoordinates,
        destination: endCoordinates
    };
}