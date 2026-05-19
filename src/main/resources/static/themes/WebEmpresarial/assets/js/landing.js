document.addEventListener('DOMContentLoaded', function () {
 
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
      data.source = 'webempresarial-landing-form';
 
      try {
        const response = await fetch('/api/leads', {
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
      const wa  = heroForm.querySelector('input[name="whatsapp"]').value.trim();
      if (!wa) return;
 
      const originalText = btn.innerText;
      btn.disabled = true;
      btn.innerText = 'Enviando...';
 
      try {
        const response = await fetch('/api/leads', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ whatsapp: wa, source: 'webempresarial-hero-micro' })
        });
        if (!response.ok) throw new Error('Error');
 
        btn.innerText = '✓ ¡Listo! Te contactamos pronto';
        btn.style.background = 'var(--green)';
        heroForm.querySelector('input').value = '';
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
 
  // ── COUNTER ANIMATION (proof bar) ─────────────────────────────────────────
  function animCount(el, to, suffix = '', decimals = 0) {
    if (!el) return;
    const duration = 1600;
    const start = performance.now();
    const run = now => {
      const progress = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      el.textContent = (to * eased).toFixed(decimals) + suffix;
      if (progress < 1) requestAnimationFrame(run);
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
 
  // ── EXIT INTENT POPUP ─────────────────────────────────────────────────────
  const overlay   = document.getElementById('exitOverlay');
  const exitClose = document.getElementById('exitClose');
  const exitSkip  = document.getElementById('exitSkip');
  const exitSubmit = document.getElementById('exitSubmit');
 
  let exitShown = false;
  // Persist dismissal for the session
  if (sessionStorage.getItem('exitShown')) exitShown = true;
 
  function showExit() {
    if (exitShown || !overlay) return;
    exitShown = true;
    sessionStorage.setItem('exitShown', '1');
    overlay.classList.add('show');
    document.body.style.overflow = 'hidden';
    if (window.gtag) gtag('event', 'exit_popup_shown');
  }
 
  function hideExit() {
    if (!overlay) return;
    overlay.classList.remove('show');
    document.body.style.overflow = '';
  }
 
  // Desktop: mouse leaves viewport top
  document.addEventListener('mouseleave', e => {
    if (e.clientY < 10) showExit();
  });
 
  // Mobile: back button / page hide after 45s of scroll
  let mobileTimer;
  window.addEventListener('scroll', () => {
    clearTimeout(mobileTimer);
    if (window.scrollY > 400) {
      mobileTimer = setTimeout(showExit, 45000);
    }
  }, { passive: true });
 
  if (exitClose) exitClose.addEventListener('click', hideExit);
  if (exitSkip)  exitSkip.addEventListener('click', hideExit);
  if (overlay)   overlay.addEventListener('click', e => { if (e.target === overlay) hideExit(); });
 
  // Close on Escape
  document.addEventListener('keydown', e => { if (e.key === 'Escape') hideExit(); });
 
  // Submit exit popup
  if (exitSubmit) {
    exitSubmit.addEventListener('click', async () => {
      const wa   = document.getElementById('exitWhatsapp')?.value.trim();
      const name = document.getElementById('exitNombre')?.value.trim();
      if (!wa) { document.getElementById('exitWhatsapp').focus(); return; }
 
      const originalText = exitSubmit.innerText;
      exitSubmit.disabled = true;
      exitSubmit.innerText = 'Enviando...';
 
      try {
        const response = await fetch('/api/leads', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ nombre: name, whatsapp: wa, source: 'webempresarial-exit-popup' })
        });
        if (!response.ok) throw new Error('Error');
 
        exitSubmit.innerText = '✓ ¡Perfecto! Te contactamos pronto';
        exitSubmit.style.background = 'var(--green)';
        if (window.gtag) gtag('event', 'generate_lead', { method: 'exit_popup' });
        if (window.fbq)  fbq('track', 'Lead', { content_name: 'exit_popup' });
        setTimeout(hideExit, 2200);
 
      } catch {
        alert('Error. Contáctanos por WhatsApp directamente.');
        exitSubmit.disabled = false;
        exitSubmit.innerText = originalText;
      }
    });
  }
 
  // ── WHATSAPP FLOAT WIDGET ─────────────────────────────────────────────────
  const waBubble  = document.getElementById('waBubble');
  const waTooltip = document.getElementById('waTooltip');
 
  if (waBubble) {
    // Show tooltip after 8s automatically
    setTimeout(() => {
      if (waTooltip) waTooltip.classList.add('show');
    }, 8000);
 
    // Hide tooltip after 14s
    setTimeout(() => {
      if (waTooltip) waTooltip.classList.remove('show');
    }, 14000);
 
    // Toggle tooltip on click/tap; redirect on second tap
    let tooltipVisible = false;
    waBubble.addEventListener('click', () => {
      // Detect context: which section is closest to viewport center
      const sections = [
        { id: 'commerce',    msg: 'Hola, me interesa el plan de Ecommerce' },
        { id: 'business',    msg: 'Hola, me interesa el plan de Página Web' },
        { id: 'systems',     msg: 'Hola, me interesa el plan de Sistemas' },
        { id: 'testimonios', msg: 'Hola, vi los casos de éxito y me interesa saber más' },
      ];
 
      const center = window.innerHeight / 2;
      let closest = { dist: Infinity, msg: 'Hola, quiero una auditoría gratuita de WebEmpresarial' };
      sections.forEach(s => {
        const el = document.getElementById(s.id);
        if (!el) return;
        const rect = el.getBoundingClientRect();
        const dist = Math.abs(rect.top + rect.height / 2 - center);
        if (dist < closest.dist) closest = { dist, msg: s.msg };
      });
 
      const waUrl = `https://wa.me/5210000000000?text=${encodeURIComponent(closest.msg)}`;
 
      if (!tooltipVisible) {
        if (waTooltip) waTooltip.classList.add('show');
        tooltipVisible = true;
        // Second click opens WA
        setTimeout(() => {
          waBubble.addEventListener('click', () => window.open(waUrl, '_blank'), { once: true });
        }, 100);
      } else {
        window.open(waUrl, '_blank');
      }
 
      if (window.gtag) gtag('event', 'wa_widget_click');
      if (window.fbq)  fbq('track', 'Contact');
    });
 
    waBubble.addEventListener('keydown', e => {
      if (e.key === 'Enter' || e.key === ' ') waBubble.click();
    });
  }
 
});