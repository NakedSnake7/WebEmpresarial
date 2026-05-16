import { $, exists } from "../core/dom.js";

export default function Countdown() {
  const hours = $('#cd-hours');
  const mins = $('#cd-mins');
  const secs = $('#cd-secs');

  if (!exists(hours) || !exists(mins) || !exists(secs)) return;

  const key = 'strideCountdownEnd';
  let end;

  const now = Date.now();
  const stored = localStorage.getItem(key);

  if (stored && parseInt(stored) > now) {
    end = parseInt(stored);
  } else {
    end = now + 72 * 60 * 60 * 1000;
    localStorage.setItem(key, end);
  }

  const tick = () => {
    const diff = end - Date.now();

    if (diff <= 0) {
      end = Date.now() + 24 * 60 * 60 * 1000;
      localStorage.setItem(key, end);
      return;
    }

    const h = Math.floor(diff / 3600000);
    const m = Math.floor((diff % 3600000) / 60000);
    const s = Math.floor((diff % 60000) / 1000);

    hours.textContent = String(h).padStart(2, '0');
    mins.textContent = String(m).padStart(2, '0');
    secs.textContent = String(s).padStart(2, '0');
  };

  tick();
  setInterval(tick, 1000);
}