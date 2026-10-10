export const cartStore = (() => {
    let products = [];
    let listeners = [];
	let coupon = null;

    function notify() {
        listeners.forEach(fn => fn(getState()));
    }

	function getState() {
	    return {
	        products: [...products],
	        coupon
	    };
	}


	function load() {
	    const saved = localStorage.getItem("cartData");

	    if (saved) {
	        const data = JSON.parse(saved);

	        products = (data.products || []).map(p => ({
	            ...p,
	            varianteId: p.varianteId ?? null
	        }));

	        coupon = data.coupon || null;
	    }

	    notify();
	}


	function save() {
	    localStorage.setItem("cartData", JSON.stringify({
	        products,
	        coupon
	    }));
	}
	
	function applyCoupon(couponData, subtotal) {
	    if (!couponData.active) {
	        throw new Error("Cupón inválido");
	    }

	    if (subtotal < (couponData.minSubtotal || 0)) {
	        throw new Error(
	            `Compra mínima $${couponData.minSubtotal}`
	        );
	    }

	    coupon = couponData;
	    save();
	    notify();
	}
	
	function removeCoupon() {
	    coupon = null;
	    save();
	    notify();
	}
	
	function getDiscount(subtotal) {
	    if (!coupon) return 0;

	    switch (coupon.type) {
	        case "PERCENT":
	            return subtotal * (coupon.value / 100);

	        case "FIXED":
	            return Math.min(coupon.value, subtotal);

	        default:
	            console.warn("Tipo de cupón desconocido:", coupon.type);
	            return 0;
	    }
	}
	
	function getTotals({ limiteEnvioGratis, costoEnvio }) {
	    const subtotal = products.reduce((s,p)=>s+p.price*p.quantity,0);
	    const discount = getDiscount(subtotal);
	    const envio = subtotal - discount >= limiteEnvioGratis ? 0 : costoEnvio;
	    const total = subtotal - discount + envio;

	    return { subtotal, discount, envio, total };
	}

	function add(product) {

	    const varianteId = product.varianteId ?? null;

	    const existing = products.find(
	        p =>
	            p.id === product.id &&
	            (p.varianteId ?? null) === varianteId
	    );

	    if (existing) {
	        existing.quantity += product.quantity;
	    } else {
	        products.push({
	            ...product,
	            varianteId
	        });
	    }

	    const subtotal = products.reduce(
	        (s, p) => s + p.price * p.quantity,
	        0
	    );

	    if (coupon && subtotal < (coupon.minSubtotal || 0)) {
	        coupon = null;
	    }

	    save();
	    notify();
	}


	function remove(productId, varianteId, qty) {
	    varianteId = varianteId ?? null;

	    const item = products.find(p =>
	        Number(p.productId ?? p.id) === Number(productId) &&
	        (p.varianteId ?? null) === varianteId
	    );

	    if (!item) {
	        console.warn("No se encontró item para eliminar:", {
	            productId,
	            varianteId,
	            products
	        });
	        return;
	    }

	    item.quantity -= qty;

	    if (item.quantity <= 0) {
	        products = products.filter(p =>
	            !(
	                Number(p.productId ?? p.id) === Number(productId) &&
	                (p.varianteId ?? null) === varianteId
	            )
	        );
	    }

	    save();
	    notify();
	}



	function clear() {
	    products = [];
	    coupon = null;
	    save();
	    notify();
	}


    function subscribe(fn) {
        listeners.push(fn);
        fn(getState());
    }

	return {
	    load,
	    add,
	    remove,
	    clear,
	    subscribe,
	    getState,
	    applyCoupon,
	    removeCoupon,
	    getDiscount,
	    getTotals   
	};

	
	
})();

