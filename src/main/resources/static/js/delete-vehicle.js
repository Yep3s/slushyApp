    document.addEventListener("DOMContentLoaded", () => {
        const botonesEliminar = document.querySelectorAll(".eliminar-vehiculo");

        botonesEliminar.forEach(boton => {
            boton.addEventListener("click", () => {
                const vehiculoId = boton.getAttribute("data-id");

                if (confirm("¿Estás seguro de que quieres eliminar este vehículo?")) {
                    fetch(`/user/vehiculos/eliminar/${vehiculoId}`, {
                        method: "DELETE"
                    })
                    .then(response => {
                        if (response.ok) {
                            alert("Vehículo eliminado correctamente.");
                            location.reload(); // Recarga la página para actualizar la lista
                        } else {
                            alert("Error al eliminar el vehículo.");
                        }
                    })
                    .catch(error => {
                        console.error("Error:", error);
                        alert("Ocurrió un error al eliminar el vehículo.");
                    });
                }
            });
        });
    });