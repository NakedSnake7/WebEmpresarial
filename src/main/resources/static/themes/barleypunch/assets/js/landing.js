document.addEventListener('DOMContentLoaded', function () {

	// ─────────────────────────────────────────────
	// LÓGICA VISUAL DE PRODUCTO
	// El carrito real vive en configurarCarrito() + cartStore.
	// Este script solo pinta precios y deja los data-* listos.
	// ─────────────────────────────────────────────
	function getPrecioMostrar(precio, precioMinimo, tieneVariantes) {
	  if (tieneVariantes && precioMinimo != null) return precioMinimo;
	  return precio;
	}

	function getPrecioConDescuento(precio, precioMinimo, tieneVariantes, tienePromocion, porcentajeDescuento) {
	  const base = getPrecioMostrar(precio, precioMinimo, tieneVariantes);
	  if (tienePromocion && porcentajeDescuento > 0 && base != null) {
	    const descuento = Math.round(base * porcentajeDescuento) / 100;
	    return Math.round((base - descuento) * 100) / 100;
	  }
	  return base;
	}

	function isSinStock(stock) {
	  return stock == null || stock <= 0;
	}

	function renderPrecioBlock(card) {
	  const precio          = parseFloat(card.dataset.precio);
	  const precioMinimo    = parseFloat(card.dataset.precioMinimo);
	  const tieneVariantes  = card.dataset.tieneVariantes === 'true';
	  const tienePromocion  = card.dataset.tienePromocion === 'true';
	  const descuento       = parseFloat(card.dataset.descuento) || 0;
	  const stock           = parseInt(card.dataset.stock) || 0;

	  const precioMostrar = getPrecioMostrar(precio, precioMinimo, tieneVariantes);
	  const precioFinal   = getPrecioConDescuento(precio, precioMinimo, tieneVariantes, tienePromocion, descuento);
	  const sinStock      = isSinStock(stock);

	  const fmt = n => n.toLocaleString('es-MX', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
	  const MXN = `<small style="font-size:0.55rem;font-family:'Oswald',sans-serif;color:rgba(240,230,200,0.4);letter-spacing:0.1em;margin-left:2px;">MXN</small>`;
	  const prefijo = tieneVariantes
	    ? `<span style="font-family:'Oswald',sans-serif;font-size:0.6rem;color:rgba(240,230,200,0.45);letter-spacing:0.1em;display:block;margin-bottom:2px;">Desde</span>`
	    : '';

	  let html = '';

	  if (sinStock) {
	    html = `<span class="beer-agotado">Agotado</span>`;
	  } else if (tienePromocion && precioFinal < precioMostrar) {
	    html = `
	      ${prefijo}
	      <div class="beer-price" style="line-height:1.1;">
	        <span style="font-family:'Oswald',sans-serif;font-size:0.7rem;color:rgba(240,230,200,0.35);text-decoration:line-through;display:block;">
	          $${fmt(precioMostrar)}
	        </span>
	        <span style="color:var(--gold);">$</span>${fmt(precioFinal)} ${MXN}
	        <span class="badge-promo-inline">−${descuento}%</span>
	      </div>`;
	  } else {
	    html = `
	      ${prefijo}
	      <div class="beer-price">
	        <span>$</span>${fmt(precioFinal)} ${MXN}
	      </div>`;
	  }

	  card.querySelector('.beer-price-block').innerHTML = html;

	  const btn = card.querySelector('.add-to-cart');
	  const qty = card.querySelector('.product-quantity');

	  if (!btn || !qty) return;

	  // Alinear data-* con configurarCarrito()
	  btn.dataset.productId = card.dataset.id;
	  btn.dataset.name = card.dataset.productName;
	  btn.dataset.price = precioFinal;
	  btn.dataset.quantityId = qty.id;
	  btn.dataset.originalStock = stock;

	  qty.dataset.productId = card.dataset.id;
	  qty.max = Math.max(stock, 0);

	  if (sinStock) {
	    btn.disabled = true;
	    btn.textContent = 'Sin stock';
	    btn.style.opacity = '0.4';
	    btn.style.cursor = 'not-allowed';
	    qty.disabled = true;
	  }
	}

	function showToast(msg, duration = 3000) {
	  const toast = document.getElementById('cartToast');
	  const msgEl = document.getElementById('toastMsg');

	  if (!toast || !msgEl) return;

	  msgEl.innerHTML = msg;

	  toast.classList.add('show');

	  clearTimeout(toast._timer);

	  toast._timer = setTimeout(() => {
	    toast.classList.remove('show');
	  }, duration);
	}

	function subscribeNewsletter() {
	  const input = document.querySelector('.newsletter-input');
	  if (input.value && input.value.includes('@')) {
	    showToast(`¡Bienvenido al Club <span>Barley Punch</span>! 🍻`, 3500);
	    input.value = '';
	  } else {
	    input.style.borderColor = '#8b2e0f';
	    setTimeout(() => input.style.borderColor = 'rgba(201,149,42,0.3)', 2000);
	  }
	}

	function toggleMenu() {
	  const btn = document.getElementById('hamburger');
	  const drawer = document.getElementById('navDrawer');
	  btn.classList.toggle('open');
	  drawer.classList.toggle('open');
	  document.body.style.overflow = drawer.classList.contains('open') ? 'hidden' : '';
	}

	function closeMenu() {
	  document.getElementById('hamburger').classList.remove('open');
	  document.getElementById('navDrawer').classList.remove('open');
	  document.body.style.overflow = '';
	}

	document.addEventListener('keydown', e => {
	  if (e.key === 'Escape') closeMenu();
	});

	const observer = new IntersectionObserver((entries) => {
	  entries.forEach((entry, i) => {
	    if (entry.isIntersecting) {
	      setTimeout(() => entry.target.classList.add('visible'), i * 80);
	    }
	  });
	}, { threshold: 0.1 });

	document.querySelectorAll('.reveal').forEach(el => observer.observe(el));
	document.querySelectorAll('.beer-card[data-id]').forEach(renderPrecioBlock);

	// Cerrar drawer visual. La apertura la controla configurarCarrito() con #cart-btn.
	document.getElementById('cartClose')?.addEventListener('click', () => {
	  document.getElementById('cartDropdown')?.classList.remove('open');
	});
});