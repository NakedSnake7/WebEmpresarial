export function configurarCategorias(root = document) {

    const sections = root.querySelectorAll("[id^='cat-']");
    const links = root.querySelectorAll(".category-scroll a");
    const scrollBar = root.querySelector(".category-scroll");
    const indicator = root.querySelector(".category-indicator");

    if (!sections.length || !links.length || !scrollBar || !indicator) return;

    /* ============================
       INDICADOR ANIMADO
       ============================ */
    function moverIndicador(btn) {
        if (!btn) return;

        const rect = btn.getBoundingClientRect();
        const containerRect = scrollBar.getBoundingClientRect();
        const newLeft = rect.left - containerRect.left + scrollBar.scrollLeft;

        const oldLeft = indicator._pos ?? newLeft;
        indicator._pos = newLeft;

        indicator.style.setProperty("--from", `${oldLeft}px`);
        indicator.style.setProperty("--to", `${newLeft}px`);
        indicator.style.width = `${rect.width}px`;

        indicator.style.animation = "bounceSlide 0.35s cubic-bezier(.25,1.5,.5,1)";
        indicator.addEventListener("animationend", () => {
            indicator.style.transform = `translateX(${newLeft}px)`;
            indicator.style.animation = "none";
        }, { once: true });
    }

    /* ============================
       CLICK EN CATEGORÍAS
       ============================ */
    links.forEach(btn => {
        btn.addEventListener("click", () => {
            const rect = btn.getBoundingClientRect();
            const containerRect = scrollBar.getBoundingClientRect();

            const offset =
                rect.left -
                containerRect.left -
                containerRect.width / 2 +
                rect.width / 2;

            scrollBar.scrollBy({ left: offset, behavior: "smooth" });
            moverIndicador(btn);
        });
    });

    /* ============================
       SCROLL VERTICAL
       ============================ */
    const onScroll = () => {
        let current = "";

        sections.forEach(section => {
            if (section.getBoundingClientRect().top <= 120) {
                current = section.id;
            }
        });

        links.forEach(link => {
            const isActive = link.getAttribute("href") === `#${current}`;
            link.classList.toggle("active", isActive);

            if (isActive) moverIndicador(link);
        });
    };

    window.addEventListener("scroll", onScroll, { passive: true });

    /* ============================
       INIT
       ============================ */
    const inicial = root.querySelector(".category-scroll .btn.active") || links[0];
    if (inicial) moverIndicador(inicial);

    onScroll();
}
