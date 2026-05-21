// Auth
import Register from "./auth/register.js";

// Cart
import "./cart/cartStore.js";
import { configurarCarrito } from "./cart/carrito.js";

document.addEventListener("DOMContentLoaded", () => {

  console.log("DOM listo");

  configurarCarrito();

  Register();

  configurarMobileMenu();

});

function configurarMobileMenu() {

  const hamburger = document.getElementById("hamburger");
  const navDrawer = document.getElementById("navDrawer");

  const drawerCartBtn =
    document.getElementById("drawer-cart-btn");

  const cartBtn =
    document.getElementById("cart-btn");

  if (!hamburger || !navDrawer) return;

  function closeMenu() {

    hamburger.classList.remove("open");

    navDrawer.classList.remove("open");

    document.body.classList.remove("menu-open");

  }

  hamburger.addEventListener("click", () => {

    hamburger.classList.toggle("open");

    navDrawer.classList.toggle("open");

    document.body.classList.toggle("menu-open");

  });

  navDrawer.querySelectorAll("a, button")
    .forEach(item => {

      item.addEventListener("click", closeMenu);

    });

  drawerCartBtn?.addEventListener("click", () => {

    closeMenu();

    cartBtn?.click();

  });

}