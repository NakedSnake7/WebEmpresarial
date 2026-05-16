import { cartStore } from './cartStore.js';


export function configurarCarrito() {
	
	if (window.__carritoInicializado) return;
	window.__carritoInicializado = true;
	
    const cartButton = document.getElementById('cartButton');
    const cartDropdown = document.getElementById('cartDropdown');
    const cartItems = document.getElementById('cartItems');
    const cartTotal = document.getElementById('cartTotal');
    const modalTotal = document.getElementById('modalTotal');
    const checkoutButton = document.getElementById('checkoutButton');
    const finalizeButton = document.getElementById("finalizeButton");
    const checkoutForm = document.getElementById('checkoutForm');
    const LIMITE_ENVIO_GRATIS = 1250;
    const COSTO_ENVIO = 120;

	
	
    let isProcessing = false;

	cartStore.load();
	
	(function limpiarCarritoPorOrdenPagada() {
	    const lastPaid = localStorage.getItem("lastPaidOrder");
	    if (lastPaid) {
	        localStorage.removeItem("cartData");
	        localStorage.removeItem("lastPaidOrder");
		cartStore.clear();
	    }
	})();

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
		  


        products.forEach(p => {
            const li = document.createElement('li');
            li.className = 'd-flex justify-content-between align-items-center mb-2';
            li.innerHTML = `
                <div><b>${p.name}</b> - $${p.price} x ${p.quantity}</div>
                <div class="d-flex align-items-center">
				<input
				  type="number"
				  class="remove-quantity"
				  min="1"
				  max="${p.quantity}"
				  value="1"
				  data-product-id="${p.id}"
				  style="width: 3rem; text-align: center; margin-right: 5px;">
					<button
					  class="btn btn-danger btn-sm remove-button"
					  data-product-id="${p.id}">
					  Eliminar
					</button>

                </div>
            `;
            cartItems.appendChild(li);
        });

		if (cartTotal) cartTotal.textContent = `$${total.toFixed(2)}`;
		if (modalTotal) modalTotal.textContent = `$${total.toFixed(2)}`;


        const cartCounter = document.getElementById('cartCounter');
        if (cartCounter) {
            const totalItems = products.reduce((sum, p) => sum + p.quantity, 0);
            cartCounter.textContent = totalItems;
        }

        // Barra de envío gratis
        const envioMensaje = document.getElementById('envioGratisMensaje');
        const envioBarra = document.getElementById('envioGratisBarra');
        const envioContainer = document.getElementById('envioGratisContainer');
		const baseEnvio = subtotal - discount;
		if (envioContainer && envioMensaje && envioBarra) {
            envioContainer.style.display = products.length ? 'block' : 'none';
			if (baseEnvio >= LIMITE_ENVIO_GRATIS) {
			  envioMensaje.textContent = "🎉 ¡Tienes envío gratis!";
			  envioBarra.style.width = "100%";
			} else {
			  const faltante = LIMITE_ENVIO_GRATIS - baseEnvio;
			  const progreso = (baseEnvio / LIMITE_ENVIO_GRATIS) * 100;
			  envioMensaje.textContent = `Agrega $${faltante.toFixed(2)} más para envío gratis`;
			  envioBarra.style.width = `${progreso.toFixed(0)}%`;
			}
        }

        if (cartDropdown) cartDropdown.style.display = products.length ? 'block' : 'none';

		const resumen = document.getElementById('cartResumenDesglose');
		if (resumen) {
		    resumen.innerHTML = `
		      <div><b>Subtotal:</b> $${subtotal.toFixed(2)}</div>
			  ${coupon ? `<div class="text-success"><b>Cupón (${coupon.code}):</b> -$${discount.toFixed(2)}</div>` : ""}
		      <div><b>Envío:</b> $${envio.toFixed(2)}</div>
			  <div class="mt-1"><b>Total:</b> $${total.toFixed(2)}</div>
			  `;}
			  
        const modalResumen = document.getElementById('modalResumenDesglose');
		if (modalResumen) {
		  modalResumen.innerHTML = `
		    <div><b>Subtotal:</b> $${subtotal.toFixed(2)}</div>
		    ${coupon ? `<div class="text-success"><b>Cupón:</b> -$${discount.toFixed(2)}</div>` : ""}
		    <div><b>Envío:</b> $${envio.toFixed(2)}</div>
			<div class="mt-1"><b>Total:</b> $${total.toFixed(2)}</div>
		  `;
		}
		}


	// FUNCION: agregar al carrito
	function addToCart(id, name, price, quantityId, originalStock) {
	    if (!id) return alert("Error: ID del producto no definido");

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
	}
	function initCouponUI() {
	  const couponInput = document.getElementById("couponInput");
	  const applyCouponBtn = document.getElementById("applyCouponBtn");
	  const couponError = document.getElementById("couponError");

	  if (!couponInput || !applyCouponBtn) return;

	  const AVAILABLE_COUPONS = {
	    WELCOME10: { code:"WELCOME10", type:"PERCENT", value:10, minSubtotal:500, active:true },
	    ENVIO50: { code:"ENVIO50", type:"FIXED", value:50, minSubtotal:800, active:true }
	  };

	  applyCouponBtn.addEventListener("click", () => {
	    try {
	      const code = couponInput.value.trim().toUpperCase();
	      const coupon = AVAILABLE_COUPONS[code];
	      if (!coupon) throw new Error("Cupón no válido");

	      const { subtotal } = cartStore.getTotals({
	        limiteEnvioGratis: LIMITE_ENVIO_GRATIS,
	        costoEnvio: COSTO_ENVIO
	      });

	      cartStore.applyCoupon(coupon, subtotal);
	      couponError.textContent = "";
	    } catch (e) {
	      couponError.textContent = e.message;
	    }
	  });
	}


	
	
	function actualizarStockSiExiste(productId) {
	    const btn = document.querySelector(
	        `.add-to-cart[data-product-id="${productId}"]`
	    );
	    if (!btn) return; // 👈 no existe = no hay error

	    const quantityId = btn.dataset.quantityId;
	    const originalStock = parseInt(btn.dataset.originalStock);

	    updateStockBadge(quantityId, originalStock);
	}


	// FUNCION: eliminar del carrito
	function removeFromCart(productId, qty) {
	    cartStore.remove(productId, qty);
	    actualizarStockSiExiste(productId);
	}


	// FUNCION: actualizar badge y stock
	function updateStockBadge(quantityId, originalStock) {
	    const input = document.getElementById(quantityId);
	    if (!input) return;

	    const productId = Number(input.dataset.productId);

	    const btn = document.querySelector(
	        `.add-to-cart[data-product-id="${productId}"]`
	    );
	    if (!btn) return;

	    // Cantidad actual en carrito (POR ID)
		const products = cartStore.getState().products;
		const inCartQtyObj = products.find(p => p.id === productId);
	    const inCartQty = inCartQtyObj ? inCartQtyObj.quantity : 0;

	    const availableStock = originalStock - inCartQty;

	    // Crear badge si no existe
	    let badge = input.parentElement.querySelector("small");
	    if (!badge) {
	        badge = document.createElement("small");
	        input.parentElement.appendChild(badge);
	    }

	    // Actualizar badge
	    if (availableStock > 10) {
	        badge.className = "ms-2 text-success";
	        badge.textContent = `Stock: ${availableStock}`;
	    } else if (availableStock > 0) {
	        badge.className = "ms-2 text-warning";
	        badge.textContent = `Stock: ${availableStock}`;
	    } else {
	        badge.className = "ms-2 text-danger";
	        badge.textContent = "Agotado";
	    }

	    // Input y botón
	    input.max = Math.max(availableStock, 0);
	    input.disabled = availableStock <= 0;
	    btn.disabled = availableStock <= 0;
	}




    // Delegación de eventos para botones "Agregar al carrito"
	document.body.addEventListener('click', function(e){
	    const btn = e.target.closest('.add-to-cart');
	    if (!btn) return;

        const id = Number(btn.dataset.productId); // 🔥 id como Long		
		const name = btn.getAttribute('data-name');
		const price = parseFloat(btn.getAttribute('data-price'));
		const quantityId = btn.getAttribute('data-quantity-id');
		const stock = parseInt(btn.getAttribute('data-original-stock'));
		
		//borrar despues
		console.log("DATA:", btn.dataset, btn.dataset.productId);

	    addToCart(id, name, price, quantityId, stock);
	});

    // Remover productos del carrito
    if (cartItems) {
        cartItems.addEventListener('click', e => {
			if (e.target.classList.contains('remove-button')) {
			    const productId = Number(e.target.dataset.productId);
			    const qty = parseInt(
			        e.target.previousElementSibling.value
			    ) || 1;

			    removeFromCart(productId, qty);
			}

        });
    }

    // Mostrar/Ocultar carrito
    if (cartButton && cartDropdown) {
        cartButton.addEventListener('click', () => {
            cartDropdown.classList.toggle('open');
        });
    }

	if (checkoutButton && checkoutModal) {
	    checkoutButton.addEventListener('click', () => {
			const modalEl = document.getElementById('checkoutModal');
			const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
			modal.show();
	    });
		checkoutModal.addEventListener('shown.bs.modal', () => {
		  precargarDatosUsuarioCheckout();
		});

	}

	// =========================
	// FINALIZAR COMPRA
	// =========================
	if (finalizeButton && checkoutForm) {
	    finalizeButton.addEventListener('click', async (e) => {
	        e.preventDefault();
			
			const paymentMethod = document.querySelector(
			    'input[name="paymentMethod"]:checked'
			)?.value;

			if (isProcessing) return;
            isProcessing = true;


			const loader = document.getElementById("loader");
			  loader.setAttribute("active", ""); // 🔥 ACTIVA EL LOADER
			  loader.shadowRoot.querySelector(".loader-text").textContent = "Procesando tu pedido...";

	       
	        finalizeButton.disabled = true;
	        finalizeButton.textContent = "Procesando...";

	        // Limpiar mensajes antiguos
	        const errorContainer = document.getElementById('checkoutErrors');
	        if (errorContainer) errorContainer.innerHTML = "";

	        // Obtener campos
	        const fullName = document.getElementById('fullName').value.trim();
			const email = document.getElementById('email').value.trim();
	        const phone = document.getElementById('phone').value.trim();
	        const address = document.getElementById('address').value.trim();

	        const errors = [];
			const products = cartStore.getState().products;

	        // Validaciones
	        if (!fullName) errors.push("Ingresa tu nombre completo.");
	        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
	            errors.push("Ingresa un correo electrónico válido.");
	        }
	        if (!/^\d{10}$/.test(phone)) {
	            errors.push("Teléfono inválido. Debe tener 10 dígitos.");
	        }
	        if (!address) errors.push("Ingresa tu dirección de envío.");
	        if (!products.length) {
	            errors.push("Tu carrito está vacío.");
	        }
			if (!paymentMethod) {
			    errors.push("Selecciona un método de pago.");
			}


	        // Mostrar errores
	        if (errors.length > 0) {
	            errors.forEach(msg => {
	                const p = document.createElement('p');
	                p.textContent = msg;
	                p.style.color = "#ff6b6b";
	                p.style.margin = "4px 0";
	                errorContainer.appendChild(p);
	            });

	            resetFinalize();
				loader.removeAttribute("active");
				loader.shadowRoot.querySelector(".loader-text").textContent =
				    "Cargando nuestros productos...";
	            return;
	        }
			


	        // Si todo OK — calcular totales
			const { subtotal, discount, envio, total } =
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




			console.log("ORDER DATA ENVIADO:", JSON.stringify(orderData, null, 2));
	        try {
				// ==========================
				// FLUJO SEGUN METODO DE PAGO
				// ==========================
				
  
				   if (paymentMethod === "TRANSFER") {

				        const res = await fetch('/api/checkout', {
				            method: 'POST',
				            headers: { 'Content-Type': 'application/json' },
				            body: JSON.stringify(orderData)
				        });

				        const data = await res.json();
				        if (!res.ok) {
				            throw new Error(data.message || "Error al crear la orden");
				        }

				        alert("¡Orden creada excitosamente, revisa tu correo!.");

				        localStorage.removeItem('cartData');
				        checkoutForm.reset();
						cartStore.clear();
						const modalEl = document.getElementById('checkoutModal');
						const modal = bootstrap.Modal.getInstance(modalEl);
						if (modal) modal.hide();
						cartDropdown.style.display = 'none';


					} else if (paymentMethod === "STRIPE") {

					    const res = await fetch('/api/checkout', {
					        method: 'POST',
					        headers: { 'Content-Type': 'application/json' },
					        body: JSON.stringify(orderData)
					    });

					    const order = await res.json();
					    if (!res.ok) {
							console.error("Respuesta backend:", order);

							throw new Error(
							  order.message ||
							  JSON.stringify(order.errors || order)
							);
					    }

					    const stripeRes = await fetch(`/api/stripe/create-session/${order.orderId}`, {
					        method: 'POST'
					    });

					    const stripeData = await stripeRes.json();
					    if (!stripeRes.ok) {
					        throw new Error(stripeData.error || "Error en Stripe");
					    }

					    window.location.href = stripeData.url;
					    return; // 🔒 CIERRE TOTAL DEL FLUJO
					}

				} catch (e) {
				    console.error(e);
				    alert(e.message || 'Error en servidor, intenta de nuevo');
				}
				
				 
				finally {
				    if (paymentMethod !== "STRIPE") {
				        loader.removeAttribute("active");
				        loader.shadowRoot.querySelector(".loader-text").textContent =
				            "Cargando nuestros productos...";
				        resetFinalize();
				    }
				}

	    }
	);
	}

	// Reset del botón
	function resetFinalize() {
	    isProcessing = false;
	    finalizeButton.disabled = false;
	    finalizeButton.textContent = "Finalizar Compra";
	}



	// ==============================
	// VALIDACIÓN EN TIEMPO REAL + BLUR
	// ==============================
	const realTimeFields = {
	    fullName: {
	        validar: value => value.trim().length > 0,
	        mensaje: "Por favor, ingresa tu nombre completo."
	    },
	    email: {
	        validar: value => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim()),
	        mensaje: "Ingresa un correo válido."
	    },
	    phone: {
	        validar: value => /^\d{10}$/.test(value.trim()),
	        mensaje: "Número inválido: deben ser 10 dígitos."
	    },
	    address: {
			validar: value => {
			    const v = value.trim();

			    if (v.length < 15) return false;
			    if (!/[A-Za-z]{3,}/.test(v)) return false; // mínimo palabras reales
			    if (!/\d{1,5}/.test(v)) return false; // número de casa
			    if (!/[,]/.test(v)) return false; // debe separar elementos con coma

			    return true;
			},
			mensaje: "Incluye calle, número, colonia, CP y ciudad. Ej: Calle 20 #102, Col. Centro, 01109, CDMX"

	    }
	};

	// Asignar eventos input + blur
	Object.keys(realTimeFields).forEach(id => {
	    const campo = document.getElementById(id);

	    // 🚫 Si el campo no existe (login, admin, etc)
	    if (!campo) return;

	    campo.addEventListener("input", () => validarCampo(id));
	    campo.addEventListener("blur", () => validarCampo(id));
	});


	function validarCampo(id) {
	    const campo = document.getElementById(id);
	    const regla = realTimeFields[id];
	    const valido = regla.validar(campo.value);
	    const errorDiv = campo.parentElement.querySelector(".invalid-feedback");

		if (!valido) {
		    campo.classList.add("is-invalid");
		    errorDiv.textContent = regla.mensaje;
		    errorDiv.style.display = "block"; // asegura que se muestre
		} else {
		    campo.classList.remove("is-invalid");
		    errorDiv.textContent = "";
		    errorDiv.style.display = "none"; // oculta el mensaje
		}

	}

    // Cargar carrito desde localStorage al inicio
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

    if (user.fullName && !fullName.value) {
      fullName.value = user.fullName;
    }
    if (user.email && !email.value) {
      email.value = user.email;
    }
    if (user.phone && !phone.value) {
      phone.value = user.phone;
    }
    if (user.address && !address.value) {
      address.value = user.address;
    }
  } catch (e) {
    console.warn("No se pudieron precargar datos del usuario");
  }
}


