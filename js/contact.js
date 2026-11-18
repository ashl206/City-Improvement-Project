const contactForm = document.getElementById("contact-form");
const contactStatus = document.getElementById("contact-status");

contactForm.addEventListener("submit", event => {
    event.preventDefault();
    contactStatus.textContent = "Thank you for your message!";
});
