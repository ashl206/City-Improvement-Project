// profile page behavior

const defaultProfile = {
    name: "User Name",
    email: "email@example.com"
};

function readStoredValue(key, fallback) {
    try {
        const value = JSON.parse(localStorage.getItem(key));
        return value ?? fallback;
    } catch {
        return fallback;
    }
}

let profile = readStoredValue("userProfile", defaultProfile);
if (typeof profile.name !== "string" || typeof profile.email !== "string") {
    profile = defaultProfile;
}

let savedRoutes = readStoredValue("savedRoutes", []);
if (!Array.isArray(savedRoutes)) savedRoutes = [];
savedRoutes = savedRoutes.filter(route => route && typeof route === "object");

// sidebar navigation
const menuButtons = document.querySelectorAll(".profile-menu-button");
const profileSections = document.querySelectorAll(".profile-section");

menuButtons.forEach(button => {
    button.addEventListener("click", () => {
        const sectionName = button.dataset.section;

        menuButtons.forEach(item => {
            item.classList.remove("active");
            item.removeAttribute("aria-current");
        });

        button.classList.add("active");
        button.setAttribute("aria-current", "page");

        profileSections.forEach(section => {
            section.classList.remove("active");
            section.hidden = true;
        });

        const selectedSection = document.getElementById(`${sectionName}-section`);
        selectedSection.hidden = false;
        selectedSection.classList.add("active");
    });
});

// display profile information
const sidebarName = document.getElementById("sidebar-name");
const sidebarEmail = document.getElementById("sidebar-email");
const sidebarInitial = document.getElementById("sidebar-initial");
const profileNameInput = document.getElementById("profile-name");
const profileEmailInput = document.getElementById("profile-email");

function displayProfile() {
    sidebarName.textContent = profile.name;
    sidebarEmail.textContent = profile.email;
    sidebarInitial.textContent = profile.name.trim().charAt(0).toUpperCase() || "U";
    profileNameInput.value = profile.name;
    profileEmailInput.value = profile.email;
}

displayProfile();

// save routes
const showSaveFormButton = document.getElementById("show-save-form");
const cancelSaveFormButton = document.getElementById("cancel-save-form");
const newRouteForm = document.getElementById("new-route-form");
const routeTitleInput = document.getElementById("new-route-title");
const routeAddressInput = document.getElementById("new-route-address");
const savedRoutesList = document.getElementById("saved-routes-list");

showSaveFormButton.addEventListener("click", () => {
    newRouteForm.hidden = false;
    routeTitleInput.focus();
});

cancelSaveFormButton.addEventListener("click", () => {
    newRouteForm.hidden = true;
    newRouteForm.reset();
});

newRouteForm.addEventListener("submit", event => {
    event.preventDefault();

    savedRoutes.push({
        id: Date.now(),
        title: routeTitleInput.value.trim(),
        address: routeAddressInput.value.trim(),
        date: new Date().toLocaleDateString()
    });

    localStorage.setItem("savedRoutes", JSON.stringify(savedRoutes));
    displaySavedRoutes();
    newRouteForm.reset();
    newRouteForm.hidden = true;
});

function displaySavedRoutes() {
    savedRoutesList.innerHTML = "";

    if (savedRoutes.length === 0) {
        savedRoutesList.innerHTML = `
            <div class="profile-empty-state">
                <div class="profile-empty-icon">⌖</div>
                <h2>No saved routes yet</h2>
                <p>Save a route to keep track of places you want to explore.</p>
            </div>
        `;
        return;
    }

    savedRoutes.forEach(route => {
        const card = document.createElement("article");
        card.className = "saved-route-card";
        card.innerHTML = `
            <div class="route-thumbnail" aria-hidden="true"></div>
            <div class="route-card-details">
                <h2></h2>
                <p class="route-address"></p>
                <p class="route-card-date"></p>
            </div>
            <div class="route-card-actions">
                <button class="route-open-button" type="button" data-action="open" data-id="${route.id}">Open</button>
                <button class="route-delete-button" type="button" aria-label="Delete saved route" data-action="delete" data-id="${route.id}">⋯</button>
            </div>
        `;

        card.querySelector("h2").textContent = route.title;
        card.querySelector(".route-address").textContent = route.address;
        card.querySelector(".route-card-date").textContent = `Saved ${route.date}`;
        savedRoutesList.appendChild(card);
    });
}

displaySavedRoutes();

savedRoutesList.addEventListener("click", event => {
    const button = event.target.closest("button");
    if (!button) return;

    const routeId = Number(button.dataset.id);
    const selectedRoute = savedRoutes.find(route => route.id === routeId);
    if (!selectedRoute) return;

    if (button.dataset.action === "open") {
        alert(`Route: ${selectedRoute.title}\nAddress: ${selectedRoute.address}\n\nRoute analysis will be connected here later.`);
    }

    if (button.dataset.action === "delete" && confirm(`Are you sure you want to delete "${selectedRoute.title}"?`)) {
        savedRoutes = savedRoutes.filter(route => route.id !== routeId);
        localStorage.setItem("savedRoutes", JSON.stringify(savedRoutes));
        displaySavedRoutes();
    }
});

// profile settings
const settingsForm = document.getElementById("settings-form");
const settingsMessage = document.getElementById("settings-message");

settingsForm.addEventListener("submit", event => {
    event.preventDefault();

    profile = {
        name: profileNameInput.value.trim() || "User Name",
        email: profileEmailInput.value.trim() || "email@example.com"
    };

    localStorage.setItem("userProfile", JSON.stringify(profile));
    displayProfile();
    settingsMessage.textContent = "Your changes have been saved!";
});
