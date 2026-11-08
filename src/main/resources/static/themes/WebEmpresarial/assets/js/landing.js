document.addEventListener('DOMContentLoaded', function () {

  // Cohorte informativa. No autoriza ni determina cobros.
  const pilotAmount = document.getElementById('pilotInitialPrivate');
  const pilotDefault = document.getElementById('pilotInitialPublic');

  const pilotCohort = new URLSearchParams(
    window.location.search
  ).get('cohort')?.toUpperCase();

  const pilotPrices = { A: 990, B: 1490 };

  if (
    pilotAmount &&
    Object.prototype.hasOwnProperty.call(pilotPrices, pilotCohort)
  ) {
    pilotAmount.textContent = new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
      maximumFractionDigits: 0
    }).format(pilotPrices[pilotCohort])
      + ' · pago inicial del piloto';

    pilotAmount.hidden = false;

    if (pilotDefault) {
      pilotDefault.hidden = true;
    }
  }

 
  // ── MENU MOBILE ──────────────────────────────────────────────────────────
  const hamburger = document.getElementById('hamburger') || document.querySelector('.hamburger');
  const mobileNav = document.getElementById('mobileNav') || document.querySelector('.mobile-nav');
 
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
  }
 
  // ── HEADER SCROLL ─────────────────────────────────────────────────────────
  const header = document.getElementById('header') || document.querySelector('header');
  if (header) {
    window.addEventListener('scroll', () => {
      header.style.background = window.scrollY > 20
        ? 'rgba(5,8,15,.97)'
        : 'rgba(5,8,15,.82)';
    }, { passive: true });
  }
 
  // ── SCROLL REVEAL ─────────────────────────────────────────────────────────
  const reveals = document.querySelectorAll('.reveal');
  if ('IntersectionObserver' in window) {
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible');
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.1, rootMargin: '0px 0px -36px 0px' });
    reveals.forEach(el => observer.observe(el));
  } else {
    reveals.forEach(el => el.classList.add('visible'));
  }
 
  // ── FAQ ACCORDION ─────────────────────────────────────────────────────────
  document.querySelectorAll('.faq-item').forEach(item => {
    const question = item.querySelector('.faq-q');
    if (!question) return;
    question.addEventListener('click', () => {
      const isOpen = item.classList.contains('open');
      document.querySelectorAll('.faq-item.open').forEach(i => i.classList.remove('open'));
      if (!isOpen) item.classList.add('open');
    });
  });
 
  // ── LEAD FORM (formulario principal) ──────────────────────────────────────
  const form = document.getElementById('leadForm');
  if (form) {
    form.addEventListener('submit', async function (e) {
      e.preventDefault();
      const button = form.querySelector('button[type="submit"]');
      const formMessage = document.getElementById('formMsg');
      if (!button) return;
 
      const originalText = button.innerText;
      button.disabled = true;
      button.innerText = 'Enviando...';
 
      const data = Object.fromEntries(new FormData(form).entries());
      const payment = data.preferenciaPagoPiloto || 'por-definir';
      data.preferenciaPagoPiloto = payment;

      const cohortHint = ['A', 'B'].includes(pilotCohort)
        ? pilotCohort : 'sin asignación';

      data.objetivo = [
        (data.objetivo || '').trim() || 'Solicita diagnóstico',
        'Preferencia de pago: ' + payment,
        'Cohorte indicada por URL (no verificada): ' + cohortHint
      ].join(' | ');

      data.source = 'webempresarial-pilot-landing';
      data.cohortHint = ['A', 'B'].includes(pilotCohort) ? pilotCohort : 'UNASSIGNED';
      data.form = 'main_form';
 
      try {
        const response = await fetch('/api/platform/prospects', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(data)
        });
        if (!response.ok) throw new Error(await response.text() || 'Error');
 
        if (formMessage) formMessage.style.display = 'block';
        form.reset();
        if (window.gtag) gtag('event', 'generate_lead', { method: 'main_form' });
        if (window.fbq)  fbq('track', 'Lead', { content_name: 'main_form' });
 
      } catch (err) {
        console.error('Lead error:', err);
        alert('Error al enviar. Contáctanos por WhatsApp.');
      } finally {
        button.disabled = false;
        button.innerText = originalText;
      }
    });
  }
 
  // ── HERO MICRO-FORM (solo WhatsApp en el hero) ────────────────────────────
  const heroForm = document.getElementById('heroMicroForm');
  if (heroForm) {
    heroForm.addEventListener('submit', async function (e) {
      e.preventDefault();
      const btn = heroForm.querySelector('button');
	  const wa = heroForm.querySelector('input[name="whatsapp"]').value.trim();
	  const nombre = heroForm.querySelector('input[name="nombre"]').value.trim();
      if (!wa) return;
 
      const originalText = btn.innerText;
      btn.disabled = true;
      btn.innerText = 'Enviando...';
 
      try {
        const response = await fetch('/api/platform/prospects', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
		  body: JSON.stringify({
		    website: heroForm.querySelector('input[name="website"]')?.value || '',
            cohortHint: ['A', 'B'].includes(pilotCohort) ? pilotCohort : 'UNASSIGNED',
            form: 'hero_micro',
            nombre,
		    whatsapp: wa,
		    objetivo: 'Solicitó diagnóstico de piloto. Cohorte URL (no verificada): ' +
              (['A', 'B'].includes(pilotCohort) ? pilotCohort : 'sin asignación'),
            source: 'webempresarial-hero-micro'
		  })        });
        if (!response.ok) throw new Error('Error');
 
        btn.innerText = '✓ ¡Listo! Te contactamos pronto';
        btn.style.background = 'var(--green)';
        heroForm.reset();
        if (window.gtag) gtag('event', 'generate_lead', { method: 'hero_micro' });
        if (window.fbq)  fbq('track', 'Lead', { content_name: 'hero_micro' });
 
        setTimeout(() => {
          btn.innerText = originalText;
          btn.style.background = '';
          btn.disabled = false;
        }, 4000);
 
      } catch {
        alert('Error al enviar. Prueba el formulario completo abajo.');
        btn.disabled = false;
        btn.innerText = originalText;
      }
    });
  }
 

  // WHATSAPP WIDGET
  const waBubble = document.getElementById('waBubble');

  if (waBubble) {
    const openPilotWhatsapp = () => {
      const msg =
        'Hola, quiero evaluar si mi negocio es compatible con el piloto de WebEmpresarial';

      window.open(
        'https://wa.me/522221521686?text=' + encodeURIComponent(msg),
        '_blank',
        'noopener,noreferrer'
      );

      if (window.gtag) gtag('event', 'wa_widget_click');
      if (window.fbq) fbq('track', 'Contact');
    };

    waBubble.addEventListener('click', openPilotWhatsapp);

    waBubble.addEventListener('keydown', e => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        openPilotWhatsapp();
      }
    });
  }

  document.querySelectorAll(".saas-plan-btn").forEach(button => {
      button.addEventListener("click", () => {
          const plan = button.dataset.plan;

          document.getElementById("saasPlan").value = plan;
          document.getElementById("saas-checkout").scrollIntoView({
              behavior: "smooth"
          });
      });
  });

  const saasForm = document.getElementById("saasCheckoutForm");

  if (saasForm) {
      saasForm.addEventListener("submit", async event => {
          event.preventDefault();

          const data = Object.fromEntries(new FormData(saasForm).entries());

          const msg = document.getElementById("saasCheckoutMsg");
          if (msg) msg.style.display = "block";

          const response = await fetch("/api/billing/checkout", {
              method: "POST",
              headers: {
                  "Content-Type": "application/json"
              },
              body: JSON.stringify(data)
          });

          if (!response.ok) {
              alert("No se pudo iniciar el checkout.");
              if (msg) msg.style.display = "none";
              return;
          }

          const result = await response.json();

          window.location.href = result.checkoutUrl;
      });
  }
  
 
});