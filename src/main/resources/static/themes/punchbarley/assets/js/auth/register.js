export default function Register() {

    const form = document.getElementById("registroForm");

    if (!form) return;

    const btn = document.getElementById("registroBtn");
    const msg = document.getElementById("registroMsg");

    form.addEventListener("submit", async (e) => {

        e.preventDefault();

        btn.disabled = true;
        btn.textContent = "Registrando...";
        msg.classList.add("d-none");

        const data = {
            fullName: form.fullName.value.trim(),
            email: form.email.value.trim(),
            phone: form.phone.value.trim() || null,
            password: form.password.value
        };

        try {

            const response = await fetch("/api/auth/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (response.ok) {

                msg.textContent = result.message || "Registro exitoso";
                msg.className = "small text-success text-center mt-2";

                // cerrar modal automáticamente
                setTimeout(() => {
                    
                    const modal = bootstrap.Modal.getInstance(
                        document.getElementById("registroModal")
                    );

                    if (modal) modal.hide();

                    location.reload();

                }, 1200);

            } else {

                msg.textContent = result.message || "Error al registrar";
                msg.className = "small text-danger text-center mt-2";

            }

        } catch (error) {

            console.error(error);

            msg.textContent = "Error de conexión";
            msg.className = "small text-danger text-center mt-2";

        } finally {

            btn.disabled = false;
            btn.textContent = "Registrarme";
            msg.classList.remove("d-none");

        }

    });

}