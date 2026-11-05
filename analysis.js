// analysis page route and saved-route behavior

const requestStorageKey = "pendingRouteRequest";
const savedRouteStorageKey = "savedRoutes";
const searchForm = document.getElementById("analysis-search-form");
const saveRouteButton = document.getElementById("save-analysis-route");
const routeInput = document.getElementById("analysis-route-input");
const destinationInput = document.getElementById("analysis-destination-input");
const routeTitle = document.getElementById("analysis-route-title");

function readPendingRequest() {
    try {
        return JSON.parse(sessionStorage.getItem(requestStorageKey)) || null;
    } catch {
        return null;
    }
}

function updateRoute(routeRequest) {
    const origin = routeRequest.origin.trim();
    const destination = routeRequest.destination.trim();

    routeInput.value = origin;
    destinationInput.value = destination;
    routeTitle.textContent = `${origin} to ${destination}`;
    saveRouteButton.disabled = false;
    saveRouteButton.innerHTML = '<span aria-hidden="true">♡</span> Save Route';
}

const pendingRequest = readPendingRequest();
const validPendingRequest = pendingRequest &&
    typeof pendingRequest.origin === "string" &&
    typeof pendingRequest.destination === "string";

if (validPendingRequest) updateRoute(pendingRequest);

searchForm.addEventListener("submit", event => {
    event.preventDefault();

    const routeRequest = {
        origin: routeInput.value.trim(),
        destination: destinationInput.value.trim(),
        requestedAt: new Date().toISOString()
    };

    if (!routeRequest.origin || !routeRequest.destination) return;

    try {
        sessionStorage.setItem(requestStorageKey, JSON.stringify(routeRequest));
    } catch {}
    updateRoute(routeRequest);
});

saveRouteButton.addEventListener("click", () => {
    const routeRequest = {
        origin: routeInput.value.trim(),
        destination: destinationInput.value.trim()
    };

    let savedRoutes = [];
    try {
        savedRoutes = JSON.parse(localStorage.getItem(savedRouteStorageKey)) || [];
        if (!Array.isArray(savedRoutes)) savedRoutes = [];
    } catch {
        alert("Could not read saved routes in this browser. Please try again.");
        return;
    }

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
            localStorage.setItem(savedRouteStorageKey, JSON.stringify(savedRoutes));
        } catch {
            alert("Could not save this route in this browser. Please try again.");
            return;
        }
    }

    saveRouteButton.disabled = true;
    saveRouteButton.innerHTML = '<span aria-hidden="true">✓</span> Route Saved';
});
