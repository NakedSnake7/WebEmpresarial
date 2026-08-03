export function configurarBotonWhatsApp() {
    const whatsappButton = document.getElementById("whatsappButton");
    if (!whatsappButton) return;

    const phoneNumber = "522221519266";

    const isCheckout = window.location.pathname.includes("checkout");

    const message = isCheckout
        ? "Hola 👋 tengo una duda antes de finalizar mi compra en WeedTlan"
        : "Hola 👋 vi sus productos y me gustaría recibir orientación";

    const url = `https://wa.me/${phoneNumber}?text=${encodeURIComponent(message)}`;

    // 👉 SOLO INSERTAR SI NO EXISTE (evita bugs)
    if (!whatsappButton.querySelector("a")) {
        whatsappButton.insertAdjacentHTML("beforeend", `
            <a href="${url}"
               target="_blank"
               rel="noopener noreferrer"
               aria-label="Contactar por WhatsApp"
               class="btn btn-success shadow whatsapp-float">

                <i class="fab fa-whatsapp fs-3"></i>

                <span class="whatsapp-tooltip">
                    ¿Te ayudo con tu compra?
                </span>
            </a>
        `);
    }

    const btn = whatsappButton.querySelector(".whatsapp-float");
    if (!btn) return;

    /* 🔥 APARECER MÁS RÁPIDO (MEJOR UX) */
    const mostrarBoton = () => {
        if (window.scrollY > 150) { // antes 400
            whatsappButton.classList.add("visible");
            window.removeEventListener("scroll", mostrarBoton);
        }
    };

    window.addEventListener("scroll", mostrarBoton);

    /* 🚀 FALLBACK: si no hacen scroll, aparece igual */
    setTimeout(() => {
        whatsappButton.classList.add("visible");
    }, 4000);

    /* 💓 PULSE INTELIGENTE */
    let interacted = false;

    btn.addEventListener("mouseenter", () => interacted = true);
    btn.addEventListener("click", () => interacted = true);

    setInterval(() => {
        if (interacted || !whatsappButton.classList.contains("visible")) return;

        btn.classList.add("pulse");
        setTimeout(() => btn.classList.remove("pulse"), 1200);
    }, 12000); // más frecuente = más conversion
}