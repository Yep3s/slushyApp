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

// Asegúrate de que esto esté en tu delete-vehicle.js (o en otro edit-vehicle.js)
// y de cargarlo **después** del HTML del modal.
// ————— GUARDAR EDICIÓN DE VEHÍCULO —————
document.getElementById('saveEditVehicleBtn').addEventListener('click', async () => {
  if (!vehiculoIdParaEditar) return;

  // 1️⃣ Preparamos el payload con los mismos nombres que tu modelo Java
  const payload = {
    placa:        document.getElementById('edit-vehicle-plate').value.trim(),
    marca:        document.getElementById('edit-vehicle-make').value.trim(),
    linea:        document.getElementById('edit-vehicle-line').value.trim(),
    modelo:       document.getElementById('edit-vehicle-model').value.trim(),
    color:        document.getElementById('edit-vehicle-color').value.trim(),
    tipoVehiculo: document.getElementById('edit-vehicle-type').value
    // el email lo inyecta tu controlador desde el JWT
  };

  try {
    // 2️⃣ Llamada al endpoint PUT, enviando la cookie JWT
    const res = await fetch(
      `/user/vehiculos/editar/${vehiculoIdParaEditar}`,
      {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload)
      }
    );
    if (!res.ok) {
      const txt = await res.text();
      throw new Error(txt || res.statusText);
    }
    const actualizado = await res.json();

    // 3️⃣ Refresca la tarjeta en el DOM con los nuevos datos
    const card = document.querySelector(`.vehicle-card[data-id="${vehiculoIdParaEditar}"]`);
    if (card) {
      // Actualiza título marca + línea
      card.querySelector('.vehicle-title').textContent =
        `${actualizado.marca} ${actualizado.linea}`;

      // Actualiza los datos dentro de .vehicle-info-item span
      const infoSpans = card.querySelectorAll('.vehicle-info-item span');
      // Asegúrate de que el orden sea: [tipoVehiculo, color, placa]
      infoSpans[0].textContent = actualizado.tipoVehiculo;
      infoSpans[1].textContent = actualizado.color;
      infoSpans[2].textContent = actualizado.placa;

      // Actualiza los data-attributes del botón de edición
      const editBtn = card.querySelector('.edit-vehicle-btn');
      editBtn.dataset.placa = actualizado.placa;
      editBtn.dataset.marca = actualizado.marca;
      editBtn.dataset.linea = actualizado.linea;
      editBtn.dataset.modelo = actualizado.modelo;
      editBtn.dataset.color = actualizado.color;
      editBtn.dataset.tipo  = actualizado.tipoVehiculo;
    }

    // 4️⃣ Cierra el modal y muestra alerta de éxito
    document.getElementById('editVehicleModal').classList.remove('active');
    showAlert('saveAlert', 'Vehículo actualizado correctamente');
    vehiculoIdParaEditar = null;

  } catch (err) {
    console.error('Error al actualizar vehículo:', err);
    showAlert('errorAlert', err.message);
  }
});

// — helper de alertas —
function showAlert(id, mensaje) {
  const a = document.getElementById(id);
  if (!a) return;
  const txt = a.querySelector('.alert-text');
  if (txt) txt.textContent = mensaje;
  a.classList.add('show');
  setTimeout(() => a.classList.remove('show'), 3000);
}



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