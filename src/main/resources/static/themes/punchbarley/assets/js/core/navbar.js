import { $, exists } from "../core/dom.js";

export default function Navbar() {
  const el = $('#navbar');
  if (!exists(el)) return;

  window.addEventListener('scroll', () => {
    el.classList.toggle('scrolled', window.scrollY > 50);
  }, { passive: true });
}