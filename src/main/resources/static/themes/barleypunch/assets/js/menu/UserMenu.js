export default function UserMenu() {

    const btn = document.getElementById("userMenuBtn");
    const menu = document.getElementById("userDropdown");

    if (!btn || !menu) return;

    btn.addEventListener("click", () => {

        const open = menu.classList.toggle("open");

        btn.setAttribute("aria-expanded", open);

    });

    // cerrar al hacer click fuera
    document.addEventListener("click", (e) => {

        if (
            !btn.contains(e.target) &&
            !menu.contains(e.target)
        ) {
            menu.classList.remove("open");
            btn.setAttribute("aria-expanded", false);
        }

    });

}