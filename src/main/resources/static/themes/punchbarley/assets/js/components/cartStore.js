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
	        products = data.products || [];
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
	    const existing = products.find(p => p.id === product.id);
	    if (existing) existing.quantity += product.quantity;
	    else products.push(product);

	    const subtotal = products.reduce((s,p)=>s+p.price*p.quantity,0);

	    if (coupon && subtotal < (coupon.minSubtotal || 0)) {
	        coupon = null;
	    }

	    save();
	    notify();
	}


	function remove(id, qty) {
	    const p = products.find(p => p.id === id);
	    if (!p) return;

	    p.quantity -= qty;
	    if (p.quantity <= 0) {
	        products = products.filter(p => p.id !== id);
	    }

	    const subtotal = products.reduce((s,p)=>s+p.price*p.quantity,0);

	    if (coupon && subtotal < (coupon.minSubtotal || 0)) {
	        coupon = null;
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

