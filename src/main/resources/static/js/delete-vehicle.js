document.addEventListener("DOMContentLoaded", function () {
    let vehiculoIdParaEditar  = null;
    let vehiculoIdParaEliminar = null;  // ← Declaración necesaria

    // ————— EDITAR VEHÍCULO —————
    document.querySelectorAll('.edit-vehicle-btn').forEach(link => {
        link.addEventListener('click', function (e) {
            e.preventDefault();
            const ds = this.dataset;
            vehiculoIdParaEditar = ds.id;

            // Rellenar formulario
            document.getElementById('edit-vehicle-plate').value = ds.placa  || '';
            document.getElementById('edit-vehicle-make').value  = ds.marca  || '';
            document.getElementById('edit-vehicle-line').value  = ds.linea  || '';
            document.getElementById('edit-vehicle-model').value = ds.modelo || '';
            document.getElementById('edit-vehicle-color').value = ds.color  || '';
            document.getElementById('edit-vehicle-type').value  = ds.tipo   || '';

            document.getElementById('editVehicleModal').classList.add('active');
        });
    });

    // Cerrar modal de edición (sólo ese)
    document.querySelectorAll('#editVehicleModal .modal-close, #editVehicleModal .modal-close-btn')
      .forEach(btn => btn.addEventListener('click', () => {
          document.getElementById('editVehicleModal').classList.remove('active');
          vehiculoIdParaEditar = null;
    }));

    // Guardar edición
    document.getElementById('saveEditVehicleBtn').addEventListener('click', function () {
        if (!vehiculoIdParaEditar) return;
        // … tu fetch PUT y lógica de actualizar tarjeta …
    });

    // ————— ELIMINAR VEHÍCULO —————
    document.querySelectorAll('.delete-vehicle-btn').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            vehiculoIdParaEliminar = this.dataset.id;
            document.getElementById('deleteVehicleModal').classList.add('active');
        });
    });

    // Confirmar eliminación
    document.getElementById('confirmDeleteVehicleBtn').addEventListener('click', function () {
        if (!vehiculoIdParaEliminar) return;

        fetch(`/user/vehiculos/eliminar/${vehiculoIdParaEliminar}`, {
            method: 'DELETE',
            credentials: 'include'
        })
        .then(res => {
            if (res.ok) {
                // Cerrar modal
                document.getElementById('deleteVehicleModal').classList.remove('active');
                showAlert('deleteAlert', 'Vehículo eliminado correctamente');

                // Quitar tarjeta y actualizar contador
                const card = document.querySelector(`.vehicle-card[data-id="${vehiculoIdParaEliminar}"]`);
                if (card) card.remove();
                const vc = document.getElementById('vehicleCount');
                const n  = parseInt(vc.textContent) || 0;
                vc.textContent = (n - 1) + ' Vehículos';

                vehiculoIdParaEliminar = null;
            } else {
                return res.text().then(t => { throw new Error(t || 'Error servidor'); });
            }
        })
        .catch(err => {
            console.error(err);
            showAlert('errorAlert', 'Error al eliminar el vehículo');
            vehiculoIdParaEliminar = null;
        });
    });

    // Cerrar modal de eliminación
    document.querySelectorAll('#deleteVehicleModal .modal-close, #deleteVehicleModal .modal-close-btn')
      .forEach(btn => btn.addEventListener('click', () => {
          document.getElementById('deleteVehicleModal').classList.remove('active');
          vehiculoIdParaEliminar = null;
    }));

    // ————— FUNCIÓN DE ALERTAS —————
    function showAlert(id, mensaje) {
        const a = document.getElementById(id);
        if (!a) return;
        const txt = a.querySelector('.alert-text');
        if (txt) txt.textContent = mensaje;
        a.classList.add('show');
        setTimeout(() => a.classList.remove('show'), 3000);
    }
});