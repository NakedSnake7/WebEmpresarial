// Core
import './core/particles.js';
import { initApp } from "./core/app.js";
import MobileMenu from "./components/mobileMenu.js";

// Auth
import Register from './auth/register.js';

//UserMenu
import UserMenu from './menu/UserMenu.js';

// Cart
import './cart/cartStore.js';
import { configurarCarrito } from './components/cart.js';

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

    configurarCarrito();
	
	Register();
	
	UserMenu();

    configurarBotonWhatsApp();

    esperarNavbar();
});


function esperarNavbar() {

    const interval = setInterval(() => {

        const navbar = document.querySelector("#navbar");

        if (navbar) {

           

            clearInterval(interval);

            MobileMenu();

        }

    }, 100);

}