import { $, exists } from "../core/dom.js";

export default function Newsletter() {
  const input = $('#newsletter-input');
  const form = $('#newsletter-form');
  const error = $('#newsletter-error');
  const success = $('#newsletter-success');
  const wrap = $('#newsletter-form-wrap');

  if (!exists(input) || !exists(form)) return;

  const validate = (email) => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  };

  const submit = () => {
    const val = input.value.trim();

    if (!validate(val)) {
      form.classList.add('error');
      error?.classList.add('show');
      input.focus();
      return;
    }

    form.classList.remove('error');
    error?.classList.remove('show');

    if (wrap) wrap.style.display = 'none';
    if (success) success.style.display = 'block';
  };

  input.addEventListener('input', () => {
    form.classList.remove('error');
    error?.classList.remove('show');
  });

  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') submit();
  });
}