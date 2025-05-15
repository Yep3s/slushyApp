// static/js/admin-servicios.js

document.addEventListener('DOMContentLoaded', () => {
  // ====== CONFIGURACIÓN GENERAL ======

  // 1. Menú responsive
  document.getElementById('menuToggle').addEventListener('click', () => {
    document.getElementById('adminSidebar').classList.toggle('active');
  });

  // 2. Pestañas
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', function() {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
      this.classList.add('active');
      document.getElementById(this.dataset.tab).classList.add('active');
    });
  });

  // 3. Helpers de modal
  const openModal = modal => modal.classList.add('active');
  const closeModal = modal => modal.classList.remove('active');

  document.querySelectorAll('.modal-close, .modal-close-btn').forEach(btn => {
    btn.addEventListener('click', () => closeModal(btn.closest('.modal')));
  });
  document.querySelectorAll('.modal').forEach(modal => {
    modal.addEventListener('click', e => {
      if (e.target === modal) closeModal(modal);
    });
  });

  // 4. Alertas
  const showAlert = (type = 'success') => {
    const id = type === 'success' ? 'saveSettingsAlert' : 'deleteItemAlert';
    const alertEl = document.getElementById(id);
    if (!alertEl) return;
    alertEl.classList.add('show');
    setTimeout(() => alertEl.classList.remove('show'), 3000);
  };

  // ====== ELEMENTOS DE SERVICIOS ======
  const addBtn           = document.getElementById('openAddServiceModal');
  const addModal         = document.getElementById('addServiceModal');
  const editModal        = document.getElementById('editServiceModal');
  const deleteModal      = document.getElementById('deleteServiceModal');

  const saveBtn          = document.getElementById('saveService');
  const updateBtn        = document.getElementById('saveServiceChanges');
  const confirmDeleteBtn = document.getElementById('confirmDeleteService');

  const tableBody        = document.querySelector('#servicesTable tbody');
  const deleteInfoDiv    = deleteModal.querySelector('.service-to-delete');

  // Campos de formulario
  const formCreate = {
    name:      document.getElementById('service-name'),
    duration:  document.getElementById('service-duration'),
    desc:      document.getElementById('service-description'),
    status:    document.getElementById('service-status'),
  };
  const formEdit = {
    name:      document.getElementById('edit-service-name'),
    duration:  document.getElementById('edit-service-duration'),
    desc:      document.getElementById('edit-service-description'),
    status:    document.getElementById('edit-service-status'),
  };

  let services = [];
  let currentId = null;  // para edición o eliminación

  // Limpia el formulario de creación
  function clearCreateForm() {
    formCreate.name.value     = '';
    formCreate.duration.value = '';
    formCreate.desc.value     = '';
    formCreate.status.value   = 'ACTIVO';
    document.querySelectorAll('#addServiceModal input[name="vehicleType"]').forEach(cb => cb.checked = false);
    document.querySelectorAll('#addServiceModal input[name^="price-"]').forEach(i => i.value = '');
  }

  // Carga desde el servidor y renderiza la tabla
  async function loadServices() {
    try {
      const res = await fetch('/admin/servicios/listarServicios', { credentials: 'include' });
      if (!res.ok) throw new Error('Error cargando servicios');
      services = await res.json();
      renderTable();
    } catch (err) {
      console.error(err);
      showAlert('danger');
    }
  }

  // Dibuja la tabla en el DOM
  function renderTable() {
    tableBody.innerHTML = '';
    services.forEach(s => {
      const precios = s.preciosPorTipo || {};
      const preciosHtml = Object.entries(precios)
        .map(([tipo, precio]) => `${tipo}: $${precio.toFixed(2)}`)
        .join('<br>');
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${s.id}</td>
        <td>${s.nombre}</td>
        <td>${s.descripcion}</td>
        <td>${s.duracionMinutos} min</td>
        <td>${preciosHtml}</td>
        <td><span class="status-badge ${s.estado.toLowerCase()}">${s.estado}</span></td>
        <td>
          <div class="table-actions">
            <a href="#" class="table-action edit" data-id="${s.id}"><i class="fas fa-edit"></i></a>
            <a href="#" class="table-action delete" data-id="${s.id}"><i class="fas fa-trash"></i></a>
          </div>
        </td>`;
      tableBody.appendChild(tr);
    });
  }

  // ====== Crear nuevo servicio ======
  addBtn.addEventListener('click', () => {
    clearCreateForm();
    openModal(addModal);
  });

  saveBtn.addEventListener('click', async e => {
    e.preventDefault();
    // recolectar tipos y precios
    const seleccionados = Array.from(document.querySelectorAll(
      '#addServiceModal input[name="vehicleType"]:checked'
    ));
    const preciosPorTipo = {};
    seleccionados.forEach(cb => {
      const tipo = cb.value;
      const inp  = document.querySelector(`#addServiceModal input[name="price-${tipo}"]`);
      preciosPorTipo[tipo] = parseFloat(inp.value) || 0;
    });
    const payload = {
      nombre:          formCreate.name.value.trim(),
      descripcion:     formCreate.desc.value.trim(),
      duracionMinutos: parseInt(formCreate.duration.value, 10),
      estado:          formCreate.status.value,
      preciosPorTipo
    };
    try {
      const res = await fetch('/admin/servicios/crearServicio', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload)
      });
      if (!res.ok) throw new Error('Error creando servicio');
      closeModal(addModal);
      showAlert('success');
      await loadServices();
    } catch (err) {
      console.error(err);
      showAlert('danger');
    }
  });

  // ====== Delegación para Editar / Eliminar fila ======
  tableBody.addEventListener('click', e => {
    e.preventDefault();
    const editBtn   = e.target.closest('.table-action.edit');
    const deleteBtn = e.target.closest('.table-action.delete');
    if (editBtn) {
      currentId = editBtn.dataset.id;
      const svc = services.find(x => x.id === currentId);
      // rellenar formulario de edición
      formEdit.name.value     = svc.nombre;
      formEdit.duration.value = svc.duracionMinutos;
      formEdit.desc.value     = svc.descripcion;
      formEdit.status.value   = svc.estado;
      // checks y precios
      document.querySelectorAll('#editServiceModal input[name="editVehicleType"]').forEach(cb => cb.checked = false);
      document.querySelectorAll('#editServiceModal input[name^="edit-price-"]').forEach(i => i.value = '');
      for (const [tipo, precio] of Object.entries(svc.preciosPorTipo || {})) {
        const cb  = document.querySelector(`#editServiceModal input[name="editVehicleType"][value="${tipo}"]`);
        const inp = document.getElementById(`edit-price-${tipo}`);
        if (cb) { cb.checked = true; inp.value = precio; }
      }
      openModal(editModal);
    }
    if (deleteBtn) {
      currentId = deleteBtn.dataset.id;
      const svc = services.find(x => x.id === currentId);
      deleteInfoDiv.innerHTML = `<strong>Servicio:</strong> ${svc.nombre}`;
      openModal(deleteModal);
    }
  });

  // ====== Guardar cambios de edición ======
  updateBtn.addEventListener('click', async e => {
    e.preventDefault();
    const seleccionados = Array.from(document.querySelectorAll(
      '#editServiceModal input[name="editVehicleType"]:checked'
    ));
    const preciosPorTipo = {};
    seleccionados.forEach(cb => {
      const tipo = cb.value;
      const inp  = document.getElementById(`edit-price-${tipo}`);
      preciosPorTipo[tipo] = parseFloat(inp.value) || 0;
    });
    const payload = {
      nombre:          formEdit.name.value.trim(),
      descripcion:     formEdit.desc.value.trim(),
      duracionMinutos: parseInt(formEdit.duration.value, 10),
      estado:          formEdit.status.value,
      preciosPorTipo
    };
    try {
      const res = await fetch(`/admin/servicios/actualizarServicio/${currentId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload)
      });
      if (!res.ok) throw new Error('Error actualizando servicio');
      closeModal(editModal);
      showAlert('success');
      await loadServices();
    } catch (err) {
      console.error(err);
      showAlert('danger');
    }
  });

  // ====== Eliminar servicio ======
  confirmDeleteBtn.addEventListener('click', async e => {
    e.preventDefault();
    try {
      const res = await fetch(`/admin/servicios/eliminarServicio/${currentId}`, {
        method: 'DELETE',
        credentials: 'include'
      });
      if (!res.ok) throw new Error('Error eliminando servicio');
      closeModal(deleteModal);
      showAlert('success');
      await loadServices();
    } catch (err) {
      console.error(err);
      showAlert('danger');
    }
  });

  // ====== Inicio ======
  loadServices();
});
