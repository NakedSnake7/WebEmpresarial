// Core
import './core/particles.js';
import { initApp } from "./core/app.js";

// Components
import Navbar from './components/navbar.js';
import MobileMenu from "./components/mobileMenu.js";

// Auth
import Register from './auth/register.js';

// User
import UserMenu from './menu/UserMenu.js';

// Cart
import { configurarCarrito } from './cart/carrito.js';

// UI
import './ui/footer-year.js';
import './ui/modal.js';
import './ui/maya-loader.js';
import './ui/sombras.js';

// Features
import { configurarBotonWhatsApp } from './whatsapp.js';


document.addEventListener("DOMContentLoaded", () => {

    console.log("DOM listo");

    initApp();

    Register();

    UserMenu();

    configurarBotonWhatsApp();

    esperarNavbar();

});


function esperarNavbar() {

    const interval = setInterval(() => {

        const navbar = document.querySelector("#navbar");

        if (!navbar) return;

        clearInterval(interval);

        // Renderiza primero
        Navbar();
        MobileMenu();

        // Espera a que el carrito exista realmente
        esperarCarrito();

    }, 100);

}

function esperarCarrito() {

    const interval = setInterval(() => {

        const cartItems = document.getElementById("cartItems");

        if (!cartItems) return;

        clearInterval(interval);

        console.log("Carrito listo");

        configurarCarrito();

    }, 50);

}