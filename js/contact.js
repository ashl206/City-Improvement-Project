const contactForm = document.getElementById("contact-form");
const contactStatus = document.getElementById("contact-status");

contactForm.addEventListener("submit", event => {
    event.preventDefault();
    contactStatus.textContent = "This form is a project demo and is not connected to a message service yet.";
});
