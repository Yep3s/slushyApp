// Botón para cancelar y volver atrás
document.getElementById("cancelBtn").addEventListener("click", () => {
    window.history.back(); // Vuelve a la vista anterior
});

// Botón para cerrar sesión
document.getElementById("logoutBtn").addEventListener("click", async () => {
    try {
        // Realiza una petición POST al backend para eliminar la cookie JWT
        const response = await fetch("/auth/logout", {
            method: "POST",
            credentials: "include" // 🔐 Importante para incluir la cookie HttpOnly
        });

        if (response.ok) {
            // Redirige al login después del logout
            window.location.href = "/login";
        } else {
            alert("No se pudo cerrar sesión correctamente.");
        }
    } catch (error) {
        console.error("Error cerrando sesión:", error);
        alert("Error al intentar cerrar sesión.");
    }
});
