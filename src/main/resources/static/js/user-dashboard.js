document.addEventListener("DOMContentLoaded", async () => {
    try {
        const response = await fetch("/auth/me", {
            method: "GET",
            credentials: "include", // 🔐 Importante para que mande la cookie JWT
        });

        if (!response.ok) {
            throw new Error("No autenticado");
        }

        const usuario = await response.json();

        document.querySelector(".user-name").textContent = `${usuario.nombre} ${usuario.apellido}`;
        document.querySelector(".user-email").textContent = usuario.email;

    } catch (error) {
        console.error("Error al cargar datos del usuario:", error);
        window.location.href = "/login"; // Redirige si no hay sesión
    }
});

// Agregar Vehiculo

document.addEventListener("DOMContentLoaded", async () => {
    try {
        const meResponse = await fetch("/auth/me", { credentials: "include" });
        if (!meResponse.ok) throw new Error("No autenticado");

        const usuario = await meResponse.json();
        document.querySelector(".user-name").textContent = `${usuario.nombre} ${usuario.apellido}`;
        document.querySelector(".user-email").textContent = usuario.email;

        // Mostrar modal al hacer clic en el contenedor "Agregar Vehículo"
        document.querySelector(".add-vehicle").addEventListener("click", () => {
            document.getElementById("addVehicleModal").classList.add("active");
        });

        // Cerrar el modal al hacer clic en los botones de cerrar
        document.querySelectorAll(".modal-close, .modal-close-btn").forEach(btn => {
            btn.addEventListener("click", () => {
                document.getElementById("addVehicleModal").classList.remove("active");
            });
        });

        // Guardar vehículo
        document.getElementById("saveVehicleBtn").addEventListener("click", async () => {
            const placa = document.getElementById("vehicle-plate").value.trim().toUpperCase();
            const marca = document.getElementById("vehicle-make").value.trim();
            const linea = document.getElementById("vehicle-line").value.trim();
            const modelo = document.getElementById("vehicle-model").value.trim();
            const color = document.getElementById("vehicle-color").value.trim();
            const tipo = document.getElementById("vehicle-type").value;

            // Validar campos
            if (!placa || !marca || !linea || !modelo || !color || !tipo) {
                alert("Por favor completa todos los campos.");
                return;
            }

            // Crear objeto vehiculo
            const vehiculo = {
                placa,
                marca,
                linea,
                modelo,
                color,
                tipoVehiculo: tipo // 👈 importante que la clave sea tipoVehiculo
            };

            try {
                const response = await fetch('http://localhost:8080/user/vehiculos/registrar', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include', // 👈 envía cookies (incluyendo jwt)
                    body: JSON.stringify(vehiculo)
                });

                if (response.ok) {
                    alert("Vehículo registrado correctamente.");
                    document.getElementById("addVehicleModal").classList.remove("active");
                    limpiarFormularioVehiculo();
                    location.reload(); // recarga para mostrar el nuevo vehículo
                } else {
                    let errorMessage = "Error desconocido.";
                    try {
                        const error = await response.json();
                        errorMessage = error.message || errorMessage;
                    } catch (jsonError) {
                        console.warn("Respuesta sin JSON.");
                    }
                    alert("Error al registrar vehículo: " + errorMessage);
                }
            } catch (error) {
                console.error("Error al enviar datos:", error);
                alert("Error de red al registrar vehículo.");
            }
        });

        function limpiarFormularioVehiculo() {
            document.getElementById("vehicle-plate").value = "";
            document.getElementById("vehicle-make").value = "";
            document.getElementById("vehicle-line").value = "";
            document.getElementById("vehicle-model").value = "";
            document.getElementById("vehicle-color").value = "";
            document.getElementById("vehicle-type").value = "";
        }

    } catch (error) {
        console.error("Error al cargar el dashboard:", error);
        window.location.href = "/login";
    }
});


