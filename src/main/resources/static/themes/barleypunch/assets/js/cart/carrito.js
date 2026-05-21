import { cartStore } from './cartStore.js';
export function configurarCarrito() {

  if (window.__carritoInicializado) return;
  window.__carritoInicializado = true;

  const cartButtons = [
    document.getElementById('cart-btn'),
    document.getElementById('cart-float-btn')
  ].filter(Boolean);

  const cartDropdown = document.getElementById('cartDropdown');
  const cartOverlay = document.getElementById('cartOverlay');
  const cartClose = document.getElementById('cartClose');

  const cartItems = document.getElementById('cartItems');
  const cartFooter = document.getElementById('cartFooter');
  const cartTotal = document.getElementById('cartTotal');
  const modalTotal = document.getElementById('modalTotal');

  const checkoutButton = document.getElementById('checkoutButton');
  const checkoutModal = document.getElementById('checkoutModal');
  const finalizeButton = document.getElementById('finalizeButton');
  const checkoutForm = document.getElementById('checkoutForm');

  const LIMITE_ENVIO_GRATIS = 1250;
  const COSTO_ENVIO = 120;

  let isProcessing = false;

  cartStore.load();

  function openCart() {
    cartDropdown?.classList.add('open');
    cartOverlay?.classList.add('open');
  }

  function closeCart() {
    cartDropdown?.classList.remove('open');
    cartOverlay?.classList.remove('open');
  }

  function toggleCart() {
    if (!cartDropdown) return;

    if (cartDropdown.classList.contains('open')) {
      closeCart();
    } else {
      openCart();
    }
  }

  function loadCart() {
    cartStore.load();
  }

  function updateCart() {
    const { products, coupon } = cartStore.getState();
    if (!cartItems) return;

    cartItems.innerHTML = '';

    const { subtotal, discount, envio, total } =
      cartStore.getTotals({
        limiteEnvioGratis: LIMITE_ENVIO_GRATIS,
        costoEnvio: COSTO_ENVIO
      });

    if (!products.length) {
      cartItems.innerHTML = `
        <div class="cart-empty-msg" id="cartEmpty">
          <span class="empty-icon">🍺</span>
          <span>Tu carrito está vacío</span>
        </div>
      `;
    }

    products.forEach(p => {
      const li = document.createElement('li');
      li.className = 'cart-item';

      li.innerHTML = `
        <div class="cart-item-info">
          <div class="cart-item-name">${p.name}</div>
          <div class="cart-item-price">$${Number(p.price).toFixed(2)} x ${p.quantity}</div>
        </div>

        <div class="cart-item-controls">
          <input
            type="number"
            class="remove-quantity"
            min="1"
            max="${p.quantity}"
            value="1"
            data-product-id="${p.id}"
            style="width:3rem;text-align:center;">

          <button
            class="cart-item-remove remove-button"
            data-product-id="${p.id}">
            ✕
          </button>
        </div>
      `;

      cartItems.appendChild(li);
    });

    if (cartFooter) {
      cartFooter.style.display = products.length ? 'flex' : 'none';
    }

    if (cartTotal) cartTotal.textContent = `$${total.toFixed(2)}`;
    if (modalTotal) modalTotal.textContent = `$${total.toFixed(2)}`;

    document.querySelectorAll('#cartCounter').forEach(counter => {
      const totalItems = products.reduce((sum, p) => sum + p.quantity, 0);
      counter.textContent = totalItems;
    });

    const envioMensaje = document.getElementById('envioGratisMensaje');
    const envioBarra = document.getElementById('envioGratisBarra');
    const envioContainer = document.getElementById('envioGratisContainer');

    const baseEnvio = subtotal - discount;

    if (envioContainer && envioMensaje && envioBarra) {
      envioContainer.style.display = products.length ? 'block' : 'none';

      if (baseEnvio >= LIMITE_ENVIO_GRATIS) {
        envioMensaje.textContent = '🎉 ¡Tienes envío gratis!';
        envioBarra.style.width = '100%';
      } else {
        const faltante = LIMITE_ENVIO_GRATIS - baseEnvio;
        const progreso = Math.min((baseEnvio / LIMITE_ENVIO_GRATIS) * 100, 100);

        envioMensaje.textContent = `Agrega $${faltante.toFixed(2)} más para envío gratis`;
        envioBarra.style.width = `${progreso.toFixed(0)}%`;
      }
    }

    const resumen = document.getElementById('cartResumenDesglose');

    if (resumen) {
      resumen.innerHTML = `
        <div class="cart-resumen-line">
          <span>Subtotal</span>
          <strong>$${subtotal.toFixed(2)}</strong>
        </div>

        ${coupon ? `
          <div class="cart-resumen-line descuento">
            <span>Cupón ${coupon.code}</span>
            <strong>-$${discount.toFixed(2)}</strong>
          </div>
        ` : ''}

        <div class="cart-resumen-line">
          <span>Envío</span>
          <strong>$${envio.toFixed(2)}</strong>
        </div>
      `;
    }

    const modalResumen = document.getElementById('modalResumenDesglose');

    if (modalResumen) {
      modalResumen.innerHTML = `
        <div><b>Subtotal:</b> $${subtotal.toFixed(2)}</div>
        ${coupon ? `<div class="text-success"><b>Cupón:</b> -$${discount.toFixed(2)}</div>` : ''}
        <div><b>Envío:</b> $${envio.toFixed(2)}</div>
        <div class="mt-1"><b>Total:</b> $${total.toFixed(2)}</div>
      `;
    }
  }

  function addToCart(id, name, price, quantityId, originalStock) {
    if (!id) return alert('Error: ID del producto no definido');

    const input = document.getElementById(quantityId);
    const qty = parseInt(input?.value) || 0;

    if (qty <= 0) return alert('Cantidad inválida');

    const products = cartStore.getState().products;
    const existing = products.find(p => p.id === id);
    const inCartQty = existing ? existing.quantity : 0;
    const availableStock = originalStock - inCartQty;

    if (qty > availableStock) {
      return alert(`Solo hay ${availableStock} unidades disponibles`);
    }

    cartStore.add({
      id,
      name,
      price,
      quantity: qty,
      quantityId
    });

    actualizarStockSiExiste(id);
    openCart();
  }

  function initCouponUI() {
    const couponInput = document.getElementById('couponInput');
    const applyCouponBtn = document.getElementById('applyCouponBtn');
    const couponError = document.getElementById('couponError');

    if (!couponInput || !applyCouponBtn) return;

    const AVAILABLE_COUPONS = {
      WELCOME10: {
        code: 'WELCOME10',
        type: 'PERCENT',
        value: 10,
        minSubtotal: 500,
        active: true
      },
      ENVIO50: {
        code: 'ENVIO50',
        type: 'FIXED',
        value: 50,
        minSubtotal: 800,
        active: true
      }
    };

    applyCouponBtn.addEventListener('click', () => {
      try {
        const code = couponInput.value.trim().toUpperCase();
        const coupon = AVAILABLE_COUPONS[code];

        if (!coupon) throw new Error('Cupón no válido');

        const { subtotal } = cartStore.getTotals({
          limiteEnvioGratis: LIMITE_ENVIO_GRATIS,
          costoEnvio: COSTO_ENVIO
        });

        cartStore.applyCoupon(coupon, subtotal);
        couponError.textContent = '';
      } catch (e) {
        couponError.textContent = e.message;
      }
    });
  }

  function actualizarStockSiExiste(productId) {
    const btn = document.querySelector(
      `.add-to-cart[data-product-id="${productId}"]`
    );

    if (!btn) return;

    const quantityId = btn.dataset.quantityId;
    const originalStock = parseInt(btn.dataset.originalStock);

    updateStockBadge(quantityId, originalStock);
  }

  function removeFromCart(productId, qty) {
    cartStore.remove(productId, qty);
    actualizarStockSiExiste(productId);
  }

  function updateStockBadge(quantityId, originalStock) {
    const input = document.getElementById(quantityId);
    if (!input) return;

    const productId = Number(input.dataset.productId);

    const btn = document.querySelector(
      `.add-to-cart[data-product-id="${productId}"]`
    );

    if (!btn) return;

    const products = cartStore.getState().products;
    const inCartQtyObj = products.find(p => p.id === productId);
    const inCartQty = inCartQtyObj ? inCartQtyObj.quantity : 0;

    const availableStock = originalStock - inCartQty;

    let badge = input.parentElement.querySelector('small');

    if (!badge) {
      badge = document.createElement('small');
      input.parentElement.appendChild(badge);
    }

    if (availableStock > 10) {
      badge.className = 'ms-2 text-success';
      badge.textContent = `Stock: ${availableStock}`;
    } else if (availableStock > 0) {
      badge.className = 'ms-2 text-warning';
      badge.textContent = `Stock: ${availableStock}`;
    } else {
      badge.className = 'ms-2 text-danger';
      badge.textContent = 'Agotado';
    }

    input.max = Math.max(availableStock, 0);
    input.disabled = availableStock <= 0;
    btn.disabled = availableStock <= 0;
  }

  document.body.addEventListener('click', function (e) {
    const btn = e.target.closest('.add-to-cart');
    if (!btn) return;

    const id = Number(btn.dataset.productId);
    const name = btn.dataset.name;
    const price = parseFloat(btn.dataset.price);
    const quantityId = btn.dataset.quantityId;
    const stock = parseInt(btn.dataset.originalStock);

    addToCart(id, name, price, quantityId, stock);
  });

  if (cartItems) {
    cartItems.addEventListener('click', e => {
      if (e.target.classList.contains('remove-button')) {
        const productId = Number(e.target.dataset.productId);
        const qtyInput = e.target.parentElement.querySelector('.remove-quantity');
        const qty = parseInt(qtyInput?.value) || 1;

        removeFromCart(productId, qty);
      }
    });
  }

  cartButtons.forEach(btn => {
    btn.addEventListener('click', toggleCart);
  });

  cartOverlay?.addEventListener('click', closeCart);
  cartClose?.addEventListener('click', closeCart);

  if (checkoutButton && checkoutModal) {
    checkoutButton.addEventListener('click', () => {
      const modal = bootstrap.Modal.getOrCreateInstance(checkoutModal);
      modal.show();
      closeCart();
    });

    checkoutModal.addEventListener('shown.bs.modal', () => {
      precargarDatosUsuarioCheckout();
    });
  }

  if (finalizeButton && checkoutForm) {
    finalizeButton.addEventListener('click', async e => {
      e.preventDefault();

      const paymentMethod = document.querySelector(
        'input[name="paymentMethod"]:checked'
      )?.value;

      if (isProcessing) return;
      isProcessing = true;

      const loader = document.getElementById('loader');

      if (loader) {
        loader.setAttribute('active', '');

        if (loader.shadowRoot?.querySelector('.loader-text')) {
          loader.shadowRoot.querySelector('.loader-text').textContent =
            'Procesando tu pedido...';
        }
      }

      finalizeButton.disabled = true;
      finalizeButton.textContent = 'Procesando...';

      const errorContainer = document.getElementById('checkoutErrors');
      if (errorContainer) errorContainer.innerHTML = '';

      const fullName = document.getElementById('fullName')?.value.trim() || '';
      const email = document.getElementById('email')?.value.trim() || '';
      const phone = document.getElementById('phone')?.value.trim() || '';
      const address = document.getElementById('address')?.value.trim() || '';

      const errors = [];
      const products = cartStore.getState().products;

      if (!fullName) errors.push('Ingresa tu nombre completo.');

      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        errors.push('Ingresa un correo electrónico válido.');
      }

      if (!/^\d{10}$/.test(phone)) {
        errors.push('Teléfono inválido. Debe tener 10 dígitos.');
      }

      if (!address) {
        errors.push('Ingresa tu dirección de envío.');
      }

      if (!products.length) {
        errors.push('Tu carrito está vacío.');
      }

      if (!paymentMethod) {
        errors.push('Selecciona un método de pago.');
      }

      if (errors.length > 0) {
        errors.forEach(msg => {
          const p = document.createElement('p');
          p.textContent = msg;
          p.style.color = '#ff6b6b';
          p.style.margin = '4px 0';
          errorContainer?.appendChild(p);
        });

        resetFinalize();
        resetLoader(loader);
        return;
      }

      const { discount, total } =
        cartStore.getTotals({
          limiteEnvioGratis: LIMITE_ENVIO_GRATIS,
          costoEnvio: COSTO_ENVIO
        });

      const { coupon } = cartStore.getState();

      const orderData = {
        customer: {
          fullName,
          email,
          phone,
          address
        },
        cart: products.map(p => ({
          productId: p.id,
          name: p.name,
          price: p.price,
          quantity: p.quantity
        })),
        paymentMethod,
        couponCode: coupon ? coupon.code : null,
        discount,
        total
      };

      try {
        if (paymentMethod === 'TRANSFER') {
          const res = await fetch('/api/checkout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderData)
          });

          const data = await res.json();

          if (!res.ok) {
            throw new Error(data.message || 'Error al crear la orden');
          }

          alert('¡Orden creada exitosamente, revisa tu correo!');

          localStorage.removeItem('cartData');
          checkoutForm.reset();
          cartStore.clear();

          const modal = bootstrap.Modal.getInstance(checkoutModal);
          if (modal) modal.hide();

          closeCart();

        } else if (paymentMethod === 'STRIPE') {
          const res = await fetch('/api/checkout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderData)
          });

          const order = await res.json();

          if (!res.ok) {
            throw new Error(
              order.message || JSON.stringify(order.errors || order)
            );
          }

          const stripeRes = await fetch(
            `/api/stripe/create-session/${order.orderId}`,
            { method: 'POST' }
          );

          const stripeData = await stripeRes.json();

          if (!stripeRes.ok) {
            throw new Error(stripeData.error || 'Error en Stripe');
          }

          window.location.href = stripeData.url;
          return;
        }
      } catch (e) {
        console.error(e);
        alert(e.message || 'Error en servidor, intenta de nuevo');
      } finally {
        if (paymentMethod !== 'STRIPE') {
          resetLoader(loader);
          resetFinalize();
        }
      }
    });
  }

  function resetFinalize() {
    isProcessing = false;
    finalizeButton.disabled = false;
    finalizeButton.textContent = 'Finalizar Compra';
  }

  function resetLoader(loader) {
    if (!loader) return;

    loader.removeAttribute('active');

    if (loader.shadowRoot?.querySelector('.loader-text')) {
      loader.shadowRoot.querySelector('.loader-text').textContent =
        'Cargando nuestros productos...';
    }
  }

  const realTimeFields = {
    fullName: {
      validar: value => value.trim().length > 0,
      mensaje: 'Por favor, ingresa tu nombre completo.'
    },
    email: {
      validar: value => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim()),
      mensaje: 'Ingresa un correo válido.'
    },
    phone: {
      validar: value => /^\d{10}$/.test(value.trim()),
      mensaje: 'Número inválido: deben ser 10 dígitos.'
    },
    address: {
      validar: value => {
        const v = value.trim();

        if (v.length < 15) return false;
        if (!/[A-Za-zÁÉÍÓÚáéíóúÑñ]{3,}/.test(v)) return false;
        if (!/\d{1,5}/.test(v)) return false;
        if (!/[,]/.test(v)) return false;

        return true;
      },
      mensaje:
        'Incluye calle, número, colonia, CP y ciudad. Ej: Calle 20 #102, Col. Centro, 01109, CDMX'
    }
  };

  Object.keys(realTimeFields).forEach(id => {
    const campo = document.getElementById(id);
    if (!campo) return;

    campo.addEventListener('input', () => validarCampo(id));
    campo.addEventListener('blur', () => validarCampo(id));
  });

  function validarCampo(id) {
    const campo = document.getElementById(id);
    const regla = realTimeFields[id];

    if (!campo || !regla) return;

    const valido = regla.validar(campo.value);
    const errorDiv = campo.parentElement.querySelector('.invalid-feedback');

    if (!errorDiv) return;

    if (!valido) {
      campo.classList.add('is-invalid');
      errorDiv.textContent = regla.mensaje;
      errorDiv.style.display = 'block';
    } else {
      campo.classList.remove('is-invalid');
      errorDiv.textContent = '';
      errorDiv.style.display = 'none';
    }
  }

  (function limpiarCarritoPorOrdenPagada() {
    const lastPaid = localStorage.getItem('lastPaidOrder');

    if (lastPaid) {
      localStorage.removeItem('cartData');
      localStorage.removeItem('lastPaidOrder');
      cartStore.clear();
    }
  })();

  loadCart();

  initCouponUI();

  cartStore.subscribe(() => {
    const products = cartStore.getState().products;

    products.forEach(p => actualizarStockSiExiste(p.id));

    updateCart();
  });
}

async function precargarDatosUsuarioCheckout() {
  try {
    const res = await fetch('/api/user/me', {
      credentials: 'include'
    });

    if (!res.ok) return;

    const user = await res.json();

    if (!user) return;

    const fullName = document.getElementById('fullName');
    const email = document.getElementById('email');
    const phone = document.getElementById('phone');
    const address = document.getElementById('address');

    if (user.fullName && fullName && !fullName.value) {
      fullName.value = user.fullName;
    }

    if (user.email && email && !email.value) {
      email.value = user.email;
    }

    if (user.phone && phone && !phone.value) {
      phone.value = user.phone;
    }

    if (user.address && address && !address.value) {
      address.value = user.address;
    }

  } catch (e) {
    console.warn('No se pudieron precargar datos del usuario');
  }
}