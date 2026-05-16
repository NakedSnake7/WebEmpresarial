document.addEventListener('DOMContentLoaded', function () {

	 // Cart
	 let cartCount = 0;
	 let cartTotal = 0;

	 function addToCart(name, price) {
	   cartCount++;
	   cartTotal += price;
	   document.getElementById('cartCount').textContent = cartCount;

	   const toast = document.getElementById('toast');
	   document.getElementById('toastMsg').innerHTML = `<span>${name}</span> agregada — Total: $${cartTotal.toLocaleString('es-MX')} MXN`;
	   toast.classList.add('show');
	   setTimeout(() => toast.classList.remove('show'), 3000);
	 }

	 function toggleCart() {
	   if (cartCount === 0) {
	     const toast = document.getElementById('toast');
	     document.getElementById('toastMsg').innerHTML = 'Tu carrito está vacío 🍺';
	     toast.classList.add('show');
	     setTimeout(() => toast.classList.remove('show'), 2500);
	   } else {
	     const toast = document.getElementById('toast');
	     document.getElementById('toastMsg').innerHTML = `${cartCount} cerveza${cartCount > 1 ? 's' : ''} — Total: <span>$${cartTotal.toLocaleString('es-MX')} MXN</span>`;
	     toast.classList.add('show');
	     setTimeout(() => toast.classList.remove('show'), 3000);
	   }
	 }

	 function subscribeNewsletter() {
	   const input = document.querySelector('.newsletter-input');
	   if (input.value && input.value.includes('@')) {
	     const toast = document.getElementById('toast');
	     document.getElementById('toastMsg').innerHTML = `¡Bienvenido al Club <span>Punch Barley</span>! 🍻`;
	     toast.classList.add('show');
	     input.value = '';
	     setTimeout(() => toast.classList.remove('show'), 3500);
	   } else {
	     input.style.borderColor = '#8b2e0f';
	     setTimeout(() => input.style.borderColor = 'rgba(201,149,42,0.3)', 2000);
	   }
	 }

	 // Scroll reveal
	 const observer = new IntersectionObserver((entries) => {
	   entries.forEach((entry, i) => {
	     if (entry.isIntersecting) {
	       setTimeout(() => entry.target.classList.add('visible'), i * 80);
	     }
	   });
	 }, { threshold: 0.1 });

	 document.querySelectorAll('.reveal').forEach(el => observer.observe(el));
});