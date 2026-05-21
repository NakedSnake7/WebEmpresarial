import { $, exists } from "../core/dom.js";

export default function ScrollIndicator() {
  const el = $('#scroll-indicator');
  if (!exists(el)) return;

  window.addEventListener('scroll', () => {
    el.classList.toggle('hidden', window.scrollY > 80);
  }, { passive: true });
}