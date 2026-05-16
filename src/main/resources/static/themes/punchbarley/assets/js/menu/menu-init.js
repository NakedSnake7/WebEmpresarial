import { configurarCategorias } from './menu-categorias.js';
import { configurarDescripciones } from './menu-descripcion.js';
import { initCarousels } from './menu-carousels.js';

export function initMenu(root = document) {

    initCarousels(root);
    configurarCategorias(root);
    configurarDescripciones(root);

    // 🔥 Menú hamburguesa
    const hamburger = root.getElementById
        ? root.getElementById("hamburger")
        : document.getElementById("hamburger");

    const navLinks = root.querySelector
        ? root.querySelector(".nav-links")
        : document.querySelector(".nav-links");

    if (hamburger && navLinks) {

        console.log("Hamburguesa encontrada");

        hamburger.addEventListener("click", () => {

            console.log("CLICK EN HAMBURGUESA");

            hamburger.classList.toggle("active");
            navLinks.classList.toggle("open");

        });

    }
}