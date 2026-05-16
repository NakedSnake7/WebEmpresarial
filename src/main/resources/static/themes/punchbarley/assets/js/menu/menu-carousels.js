export function initCarousels() {
    document.querySelectorAll('.carousel').forEach(carousel => {
        new bootstrap.Carousel(carousel, {
            interval: 3500,
            ride: 'carousel',
            pause: false,
            wrap: true
        });
    });
}
