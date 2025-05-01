document.addEventListener("DOMContentLoaded", function () {
    let vehiculoIdParaEditar = null;

    // Abrir modal y cargar datos del vehículo
    document.querySelectorAll('.edit-vehicle-btn').forEach(link => {
        link.addEventListener('click', function (e) {
            e.preventDefault();

            const target = e.currentTarget; // Siempre el <a>
            const dataset = target.dataset;

            vehiculoIdParaEditar = dataset.id;
            console.log("Vehículo a editar:", dataset);

            document.getElementById('edit-vehicle-plate').value = dataset.placa || '';
            document.getElementById('edit-vehicle-make').value = dataset.marca || '';
            document.getElementById('edit-vehicle-line').value = dataset.linea || '';
            document.getElementById('edit-vehicle-model').value = dataset.modelo || '';
            document.getElementById('edit-vehicle-color').value = dataset.color || '';
            document.getElementById('edit-vehicle-type').value = dataset.tipo || '';

            document.getElementById('editVehicleModal').classList.add('active');
        });
    });

    // Cerrar el modal
    document.querySelectorAll('.modal-close, .modal-close-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.getElementById('editVehicleModal').classList.remove('active');
        });
    });

    // Guardar cambios del vehículo
    document.getElementById('saveEditVehicleBtn').addEventListener('click', function () {
        if (!vehiculoIdParaEditar) return;

        const placa = document.getElementById('edit-vehicle-plate').value;
        const marca = document.getElementById('edit-vehicle-make').value;
        const linea = document.getElementById('edit-vehicle-line').value;
        const modelo = document.getElementById('edit-vehicle-model').value;
        const color = document.getElementById('edit-vehicle-color').value;
        const tipo = document.getElementById('edit-vehicle-type').value;

        fetch(`/user/vehiculos/editar/${vehiculoIdParaEditar}`, {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('jwtToken'),
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                placa: placa,
                marca: marca,
                linea: linea,
                modelo: modelo,
                tipoVehiculo: tipo,
                color: color
            })
        })
        .then(response => {
            if (response.ok) {
                // Cerrar modal y mostrar alerta
                document.getElementById('editVehicleModal').classList.remove('active');
                showAlert('editAlert', 'Vehículo actualizado correctamente');

                // Puedes actualizar los datos visualmente aquí si lo deseas
                // location.reload(); // o actualizar dinámicamente si prefieres
            } else {
                return response.text().then(text => { throw new Error(text); });
            }
        })
        .catch(error => {
            console.error('Error al actualizar vehículo:', error);
            alert("Error al actualizar el vehículo.");
        });
    });

    // Cerrar modal sin guardar cambios
    document.querySelectorAll('.modal-close, .modal-close-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.getElementById('editVehicleModal').classList.remove('active');
            vehiculoIdParaEditar = null;
        });
    });

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
