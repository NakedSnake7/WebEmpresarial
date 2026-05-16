export default function Particles() {
  const canvas = document.getElementById('particles');
  if (!canvas) return;

  const ctx = canvas.getContext('2d');
  
 particlesJS("particles-js", {
  "particles": {
    "number": {
      "value": 90,
      "density": { "enable": true, "value_area": 900 }
    },
    "color": {
      "value": ["#00ffcc", "#2bff99", "#00e6b8"]
    },
    "shape": { "type": "circle" },
    "opacity": {
      "value": 0.35,
      "random": true,
      "anim": {
        "enable": true,
        "speed": 0.8,
        "opacity_min": 0.05,
        "sync": false
      }
    },
    "size": {
      "value": 4,
      "random": true,
      "anim": {
        "enable": true,
        "speed": 2,
        "size_min": 0.6,
        "sync": false
      }
    },
    "line_linked": {
      "enable": true,
      "distance": 150,
      "color": "#00ffcc",
      "opacity": 0.35,
      "width": 1
    },
    "move": {
      "enable": true,
      "speed": 1.3,
      "direction": "none",
      "random": true,
      "straight": false,
      "out_mode": "out",
      "bounce": false
    }
  },
  "interactivity": {
    "detect_on": "canvas",
    "events": {
      "onhover": { "enable": true, "mode": "grab" },
      "onclick": { "enable": false },
      "resize": true
    },
    "modes": {
      "grab": { 
        "distance": 200,
        "line_linked": { "opacity": 0.8 }
      }
    }
  },
  "retina_detect": true
});

}