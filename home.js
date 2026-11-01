// route form info to carry on to the analysis page
const routeForm = document.getElementById("route-form");

routeForm.addEventListener("submit", event => {
    event.preventDefault();

    const routeRequest = {
        origin: document.getElementById("start-location").value.trim(),
        destination: document.getElementById("end-location").value.trim(),
        requestedAt: new Date().toISOString()
    };

    if (!routeRequest.origin || !routeRequest.destination) return;

    try {
        sessionStorage.setItem("pendingRouteRequest", JSON.stringify(routeRequest));
    } catch {
    }
    window.location.href = "analysis.html";
});
