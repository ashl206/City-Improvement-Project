// analysis page route and saved-route behavior

// import the map function from script.js
import { displayAddressRoute } from "./script.js";


// storage keys
const requestStorageKey = "pendingRouteRequest";
const savedRouteStorageKey = "savedRoutes";


// HTML elements
const searchForm = document.getElementById("analysis-search-form");
const saveRouteButton = document.getElementById("save-analysis-route");

const routeInput = document.getElementById("analysis-route-input");
const destinationInput = document.getElementById("analysis-destination-input");

const routeTitle = document.getElementById("analysis-route-title");
const analysisSummary = document.getElementById("analysis-summary");


// read pending route
function readPendingRequest() {

    try {
        return JSON.parse(sessionStorage.getItem(requestStorageKey)) || null;
    } catch {
        return null;
    }
}


// update route info
function updateRoute(routeRequest) {

    const origin = routeRequest.origin.trim();
    const destination = routeRequest.destination.trim();

    routeInput.value = origin;
    destinationInput.value = destination;

    routeTitle.textContent = `${origin} to ${destination}`;

    saveRouteButton.disabled = true;

    saveRouteButton.innerHTML =
        '<span aria-hidden="true">♡</span> Save Route';
}


// analyze the route
async function runAnalysis(routeRequest) {

    updateRoute(routeRequest);

    analysisSummary.textContent = "Finding your locations...";

    try {

        // get coordinates and display the maps
        const coordinates = await displayAddressRoute(
            routeRequest.origin,
            routeRequest.destination
        );

        console.log("Route coordinates:", coordinates);

        analysisSummary.textContent =
            "Your starting point and destination have been located! " +
            "CURRENT PLACEHOLDER TEXT - WALKING ROUTES AND IMPROVEMENT WILL BE ADDED W AI HERE";

        // allow the user to save the route
        saveRouteButton.disabled = false;

    } catch (error) {
        console.error("Route analysis error:", error);
        analysisSummary.textContent = error.message;
        saveRouteButton.disabled = true;
    }
}


// load route from homepage

const pendingRequest = readPendingRequest();

const validPendingRequest = pendingRequest &&
    typeof pendingRequest.origin === "string" &&
    typeof pendingRequest.destination === "string";

if (validPendingRequest) {

    runAnalysis(pendingRequest);

}


// search for a new route

searchForm.addEventListener("submit", async event => {

    event.preventDefault();

    const routeRequest = {
        origin: routeInput.value.trim(),
        destination: destinationInput.value.trim(),
        requestedAt: new Date().toISOString()

    };

    if (!routeRequest.origin || !routeRequest.destination) {
        return;
    }

    // save the search temporarily
    try {
        sessionStorage.setItem(
            requestStorageKey,
            JSON.stringify(routeRequest)
        );

    } catch {
        console.error("Could not save the pending route.");
    }

    // start the analysis
    await runAnalysis(routeRequest);

});


// save route
saveRouteButton.addEventListener("click", () => {

    const routeRequest = {
        origin: routeInput.value.trim(),
        destination: destinationInput.value.trim()

    };


    let savedRoutes = [];

    try {
        savedRoutes = JSON.parse(
            localStorage.getItem(savedRouteStorageKey)
        ) || [];

        if (!Array.isArray(savedRoutes)) {
            savedRoutes = [];
        }

    } catch {
        alert("Could not read saved routes in this browser. Please try again.");
        return;

    }


    // check whether the route was already saved
    const alreadySaved = savedRoutes.some(route =>

        route &&
        route.origin === routeRequest.origin &&
        route.destination === routeRequest.destination

    );

    if (!alreadySaved) {
        savedRoutes.push({
            id: Date.now(),
            title: `${routeRequest.origin} to ${routeRequest.destination}`,
            address: `${routeRequest.origin} → ${routeRequest.destination}`,
            origin: routeRequest.origin,
            destination: routeRequest.destination,
            date: new Date().toLocaleDateString()
        });


        try {
            localStorage.setItem(
                savedRouteStorageKey,
                JSON.stringify(savedRoutes)
            );

        } catch {
            alert("Could not save this route in this browser. Please try again.");
            return;
        }

    }

    saveRouteButton.disabled = true;
    saveRouteButton.innerHTML =
        '<span aria-hidden="true">✓</span> Route Saved';

});
