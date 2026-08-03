import Navbar from "../components/navbar.js";
import ScrollIndicator from "../components/scrollIndicator.js";
import Countdown from "../components/countdown.js";
import Newsletter from "../components/newsletter.js";
import ScrollReveal from "../utils/scrollReveal.js";




export function initApp() {
  try {
    Navbar();
    ScrollIndicator();
    Countdown();
    Newsletter();
    ScrollReveal();
  } catch (e) {
    console.error("Error inicializando app:", e);
  }
}