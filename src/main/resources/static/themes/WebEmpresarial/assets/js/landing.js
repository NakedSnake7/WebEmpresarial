document.addEventListener('DOMContentLoaded', function () {

	// MENU MOBILE
	const hamburger =
	  document.getElementById('hamburger') ||
	  document.querySelector('.hamburger');

	const mobileNav =
	  document.getElementById('mobileNav') ||
	  document.querySelector('.mobile-nav');

	console.log('hamburger:', hamburger);
	console.log('mobileNav:', mobileNav);

	if (hamburger && mobileNav) {
	  hamburger.addEventListener('click', () => {
	    const isOpen = mobileNav.classList.toggle('open');

	    hamburger.classList.toggle('open', isOpen);
	    hamburger.setAttribute('aria-expanded', String(isOpen));
	    mobileNav.setAttribute('aria-hidden', String(!isOpen));
	  });

	  document.querySelectorAll('.mobile-link, .ml').forEach(link => {
	    link.addEventListener('click', () => {
	      hamburger.classList.remove('open');
	      mobileNav.classList.remove('open');
	      hamburger.setAttribute('aria-expanded', 'false');
	      mobileNav.setAttribute('aria-hidden', 'true');
	    });
	  });
	} else {
	  console.warn('No se encontró hamburger o mobileNav');
	}

  // HEADER SCROLL
  const header = document.getElementById('header') || document.querySelector('header');

  if (header) {
    window.addEventListener('scroll', () => {
      header.style.background = window.scrollY > 20
        ? 'rgba(5,8,15,.97)'
        : 'rgba(5,8,15,.82)';
    }, { passive: true });
  }

  // REVEAL
  const reveals = document.querySelectorAll('.reveal');

  if ('IntersectionObserver' in window) {
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible');
          observer.unobserve(entry.target);
        }
      });
    }, {
      threshold: 0.1,
      rootMargin: '0px 0px -36px 0px'
    });

    reveals.forEach(el => observer.observe(el));
  } else {
    reveals.forEach(el => el.classList.add('visible'));
  }

  // FAQ
  document.querySelectorAll('.faq-item').forEach(item => {
    const question = item.querySelector('.faq-q');

    if (!question) return;

    question.addEventListener('click', () => {
      const isOpen = item.classList.contains('open');

      document.querySelectorAll('.faq-item.open')
        .forEach(i => i.classList.remove('open'));

      if (!isOpen) item.classList.add('open');
    });
  });

  // FORM
  const form = document.getElementById('leadForm');

  if (form) {
    form.addEventListener('submit', async function (e) {
      e.preventDefault();

      const button = form.querySelector('button[type="submit"]');
      const formMessage = document.getElementById('formMsg');

      if (!button) return;

      const originalButtonText = button.innerText;

      button.disabled = true;
      button.innerText = 'Enviando...';

      const data = Object.fromEntries(new FormData(form).entries());
      data.source = 'webempresarial-landing';

      try {
        const response = await fetch('/api/leads', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(data)
        });

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(errorText || 'Error al enviar lead');
        }

        if (formMessage) {
          formMessage.style.display = 'block';
        }

        form.reset();

        if (window.gtag) gtag('event', 'generate_lead');
        if (window.fbq) fbq('track', 'Lead');

      } catch (err) {
        console.error('Lead error:', err);
        alert('Error al enviar. Contáctanos por WhatsApp.');
      } finally {
        button.disabled = false;
        button.innerText = originalButtonText;
      }
    });
  }

  // COUNTER ANIMATION
  function animCount(el, to, suffix = '', decimals = 0) {
    if (!el) return;

    const duration = 1600;
    const start = performance.now();

    const run = now => {
      const progress = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      const value = (to * eased).toFixed(decimals);

      el.textContent = value + suffix;

      if (progress < 1) {
        requestAnimationFrame(run);
      }
    };

    requestAnimationFrame(run);
  }

  const proofBar = document.querySelector('.proof-bar');

  if (proofBar && 'IntersectionObserver' in window) {
    const proofObserver = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          animCount(document.getElementById('pn1'), 40, '+');
          animCount(document.getElementById('pn2'), 98, '%');
          proofObserver.unobserve(entry.target);
        }
      });
    }, { threshold: 0.5 });

    proofObserver.observe(proofBar);
  }
});