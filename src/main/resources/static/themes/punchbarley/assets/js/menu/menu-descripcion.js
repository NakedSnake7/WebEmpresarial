export function configurarDescripciones() {
    document.addEventListener('click', e => {
        const link = e.target.closest('.ver-mas');
        if (!link) return;

        e.preventDefault();

        const tarjeta = link.closest('.maya-card');
        const descripcion = tarjeta?.querySelector('.descripcion-corta');
        if (!descripcion) return;

        const estaExpandida = descripcion.classList.contains('expandida');

        const todasDescripciones = document.querySelectorAll('.descripcion-corta');
        const todosLosBotones = document.querySelectorAll('.ver-mas');

        // Cerrar todas
        todasDescripciones.forEach(desc => desc.classList.remove('expandida'));
        todosLosBotones.forEach(btn => {
            btn.classList.remove('expandido');
            btn.textContent = 'Ver más';
        });

        if (!estaExpandida) {
            // Abrir actual
            descripcion.classList.add('expandida');
            link.classList.add('expandido');
            link.textContent = 'Ver menos';
        } else {
            // Scroll suave y highlight
            setTimeout(() => {
                tarjeta.scrollIntoView({ behavior: 'smooth', block: 'start' });
                tarjeta.classList.add('resaltado');
                setTimeout(() => tarjeta.classList.remove('resaltado'), 1500);
            }, 200);
        }
    });
}
