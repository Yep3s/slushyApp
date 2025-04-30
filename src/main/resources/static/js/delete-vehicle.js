document.addEventListener("DOMContentLoaded", function () {
    let vehiculoIdParaEliminar = null;

    // Cuando el usuario hace clic en el icono de eliminar
    document.querySelectorAll('.delete-vehicle-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            vehiculoIdParaEliminar = this.dataset.id;
            document.getElementById('deleteVehicleModal').classList.add('active');
        });
    });

    // Confirmar eliminación del vehículo
    document.getElementById('confirmDeleteVehicleBtn').addEventListener('click', function() {
        if (!vehiculoIdParaEliminar) return;

        fetch(`/user/vehiculos/eliminar/${vehiculoIdParaEliminar}`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('jwtToken'),
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                document.getElementById('deleteVehicleModal').classList.remove('active');
                showAlert('deleteAlert', 'Vehículo eliminado correctamente');

                // Elimina visualmente la tarjeta del vehículo
                const card = document.querySelector(`.vehicle-card[data-id="${vehiculoIdParaEliminar}"]`);
                if (card) card.remove();

                // Actualizar contador visual
                const vehicleCountElement = document.getElementById('vehicleCount');
                const currentCount = parseInt(vehicleCountElement.textContent) || 0;
                if (currentCount > 0) {
                    vehicleCountElement.textContent = (currentCount - 1) + ' Vehículos';
                }

                vehiculoIdParaEliminar = null;
            } else {
                return response.text().then(text => { throw new Error(text); });
            }
        })
        .catch(error => {
            console.error('Error al eliminar vehículo:', error);
            alert("Error al eliminar el vehículo.");
        });
    });

    // Cerrar modal sin eliminar
    document.querySelectorAll('.modal-close-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.getElementById('deleteVehicleModal').classList.remove('active');
            vehiculoIdParaEliminar = null;
        });
    });
});
