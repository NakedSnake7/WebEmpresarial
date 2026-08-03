document.addEventListener("DOMContentLoaded", () => {
    const contenedor = document.getElementById("reseñas-container");
    const btnVerMas = document.getElementById("btnVerMas");

    let todasReseñas = [];
    let indiceActual = 0;
    let cargandoFragmento = false;

    /* =========================
       UTIL: esperar imágenes
    ========================= */
    function esperarImagenes(elementos, callback) {
        const imgs = [];
        elementos.forEach(el => {
            imgs.push(...el.querySelectorAll("img"));
        });

        let cargadas = 0;
        if (imgs.length === 0) {
            callback();
            return;
        }

        imgs.forEach(img => {
            if (img.complete) {
                cargadas++;
            } else {
                img.onload = img.onerror = () => {
                    cargadas++;
                    if (cargadas === imgs.length) callback();
                };
            }
        });

        if (cargadas === imgs.length) callback();
    }

    /* =========================
       BLOQUE RESPONSIVO
    ========================= */
    function calcularBloque() {
        const ancho = window.innerWidth;
        if (ancho >= 1200) return 6;
        if (ancho >= 992) return 5;
        if (ancho >= 768) return 4;
        return 3;
    }

    /* =========================
       MOSTRAR BLOQUE
    ========================= */
    function mostrarBloque() {
        const BLOQUE = calcularBloque();
        const bloque = todasReseñas.slice(indiceActual, indiceActual + BLOQUE);

        if (bloque.length === 0) return;

        // Insertar cards (sin animar todavía)
        bloque.forEach(card => {
            card.classList.remove("in-view");
            card.style.setProperty("--delay", "0ms");
            contenedor.appendChild(card);
        });

        // Esperar imágenes → animar aparición
        esperarImagenes(bloque, () => {
            bloque.forEach((card, i) => {
                card.style.setProperty("--delay", `${i * 60}ms`);
                requestAnimationFrame(() => {
                    card.classList.add("in-view");
                });
            });
        });

        indiceActual += bloque.length;

        if (indiceActual >= todasReseñas.length) {
            btnVerMas.style.display = "none";
        }
    }


    /* =========================
       CARGAR FRAGMENTO
    ========================= */
    function cargarReseñasFragmento() {
        if (cargandoFragmento) return;
        cargandoFragmento = true;

        fetch("/fragmento-resenas")
            .then(resp => {
                if (!resp.ok) throw new Error("No se pudo cargar el fragmento");
                return resp.text();
            })
            .then(html => {
                const tempDiv = document.createElement("div");
                tempDiv.innerHTML = html;
                todasReseñas = Array.from(tempDiv.querySelectorAll(".reseña"));

                if (todasReseñas.length === 0) {
                    console.warn("No hay reseñas en el fragmento.");
                    return;
                }
                
                indiceActual = 0; // ✅ FIX CLAVE
                mostrarBloque();
            })
            .catch(err => console.error("Error al cargar reseñas:", err))
            .finally(() => cargandoFragmento = false);
    }

    /* =========================
       EVENTOS
    ========================= */
    btnVerMas.addEventListener("click", () => {
        if (todasReseñas.length === 0) {
            cargarReseñasFragmento();
        } else {
            mostrarBloque();
        }
    });

    window.addEventListener("scroll", () => {
        const scrollPos = window.scrollY + window.innerHeight;
        const threshold = document.body.offsetHeight - 300;

        if (scrollPos >= threshold &&
            todasReseñas.length > 0 &&
            indiceActual < todasReseñas.length) {
            mostrarBloque();
        }
    });
});
