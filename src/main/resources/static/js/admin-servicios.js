// static/js/admin-servicios.js

document.addEventListener('DOMContentLoaded', function() {
  // ====== GENERIC ADMIN BEHAVIOR ======

  // 1. Menú responsive
  const menuToggle = document.getElementById('menuToggle');
  menuToggle.addEventListener('click', () => {
    document.getElementById('adminSidebar').classList.toggle('active');
  });

  // 2. Pestañas
  const tabBtns     = document.querySelectorAll('.tab-btn');
  const tabContents = document.querySelectorAll('.tab-content');
  tabBtns.forEach(btn => {
    btn.addEventListener('click', function() {
      tabBtns.forEach(b => b.classList.remove('active'));
      tabContents.forEach(c => c.classList.remove('active'));
      this.classList.add('active');
      document.getElementById(this.dataset.tab).classList.add('active');
    });
  });

  // 3. Modales
  function showModal(modal) { modal.classList.add('active'); }
  function hideModal(modal) { modal.classList.remove('active'); }

  document.querySelectorAll('.modal-close, .modal-close-btn').forEach(btn => {
    btn.addEventListener('click', () => hideModal(btn.closest('.modal')));
  });
  document.querySelectorAll('.modal').forEach(modal => {
    modal.addEventListener('click', e => {
      if (e.target === modal) hideModal(modal);
    });
  });

  // 4. Alertas
  function showAlert(type = 'success', specificId = '') {
    let alertEl;
    if (specificId) {
      alertEl = document.getElementById(specificId);
    } else {
      alertEl = type === 'success'
        ? document.getElementById('saveSettingsAlert')
        : document.getElementById('deleteItemAlert');
    }
    if (!alertEl) return;
    alertEl.classList.add('show');
    setTimeout(() => alertEl.classList.remove('show'), 3000);
  }

  // ====== SERVICE-SPECIFIC BEHAVIOR ======

  const openAddBtn       = document.getElementById('openAddServiceModal');
  const addModal         = document.getElementById('addServiceModal');
  const editModal        = document.getElementById('editServiceModal');
  const deleteModal      = document.getElementById('deleteServiceModal');

  const saveAddBtn       = document.getElementById('saveService');
  const saveEditBtn      = document.getElementById('saveServiceChanges');
  const confirmDeleteBtn = document.getElementById('confirmDeleteService');

  // Form fields (create)
  const serviceName      = document.getElementById('service-name');
  const serviceDuration  = document.getElementById('service-duration');
  const serviceDesc      = document.getElementById('service-description');
  const serviceStatus    = document.getElementById('service-status');

  // Form fields (edit)
  const editName         = document.getElementById('edit-service-name');
  const editDuration     = document.getElementById('edit-service-duration');
  const editDesc         = document.getElementById('edit-service-description');
  const editStatus       = document.getElementById('edit-service-status');

  const deleteInfoDiv    = deleteModal.querySelector('.service-to-delete');
  const tbody            = document.querySelector('#servicesTable tbody');

  let allServices = [];
  let selectedId  = null;

  function clearAddForm() {
    serviceName.value     = '';
    serviceDuration.value = '';
    serviceDesc.value     = '';
    serviceStatus.value   = 'ACTIVO';
    // desmarcar y limpiar precios
    document.querySelectorAll('#addServiceModal input[name="vehicleType"]').forEach(cb => cb.checked = false);
    document.querySelectorAll('#addServiceModal input[name^="price-"]').forEach(inp => inp.value = '');
  }

  async function loadServices() {
    try {
      const resp = await fetch('/admin/servicios/listarServicios', { credentials: 'include' });
      if (!resp.ok) throw new Error('Error cargando servicios');
      allServices = await resp.json();
      renderTable(allServices);
    } catch (err) {
      console.error(err);
      showAlert('danger');
    }
  }

  function renderTable(services) {
    tbody.innerHTML = '';
    services.forEach(s => {
      const precios = s.preciosPorTipo || {};
      const preciosHtml = Object.entries(precios)
        .map(([tipo, precio]) => `${tipo}: $${precio.toFixed(2)}`)
        .join('<br>');

      const tr = document.createElement('tr');
      tr.dataset.id = s.id;
      tr.innerHTML = `
        <td>${s.id}</td>
        <td>${s.nombre}</td>
        <td>${s.descripcion}</td>
        <td>${s.duracionMinutos} min</td>
        <td>${preciosHtml}</td>
        <td><span class="status-badge ${s.estado.toLowerCase()}">${s.estado}</span></td>
        <td>
          <div class="table-actions">
            <a href="#" class="table-action" title="Editar"><i class="fas fa-edit"></i></a>
            <a href="#" class="table-action" title="Eliminar"><i class="fas fa-trash"></i></a>
          </div>
        </td>`;
      tbody.appendChild(tr);
    });
  }

  // Crear servicio
  openAddBtn.addEventListener('click', () => {
    clearAddForm();
    showModal(addModal);
  });

  saveAddBtn.addEventListener('click', async e => {
    e.preventDefault();
    // recolectar tipos y precios
    const seleccionados = Array.from(document.querySelectorAll('#addServiceModal input[name="vehicleType"]:checked'));
    const preciosPorTipo = {};
    seleccionados.forEach(cb => {
      const tipo = cb.value;
      const inp  = document.querySelector(`#addServiceModal input[name="price-${tipo}"]`);
      preciosPorTipo[tipo] = parseFloat(inp.value) || 0;
    });

    const payload = {
      nombre:          serviceName.value.trim(),
      descripcion:     serviceDesc.value.trim(),
      duracionMinutos: parseInt(serviceDuration.value, 10),
      estado:          serviceStatus.value,
      preciosPorTipo:  preciosPorTipo
    };

    try {
      const resp = await fetch('/admin/servicios/crearServicio', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload)
      });
      if (!resp.ok) throw new Error('Error creando servicio');
      hideModal(addModal);
      showAlert('success');
      await loadServices();
    } catch (err) {
      console.error(err);
      showAlert('danger');
    }
  });

  // Editar / eliminar delegados
  tbody.addEventListener('click', e => {
    const tr    = e.target.closest('tr');
    if (!tr) return;
    selectedId = tr.dataset.id;
    const svc  = allServices.find(s => s.id === selectedId);
    const editA = e.target.closest('a[title="Editar"]');
    const delA  = e.target.closest('a[title="Eliminar"]');

    if (editA) {
      // precargar formulario de edición
      editName.value     = svc.nombre;
      editDesc.value     = svc.descripcion;
      document.getElementById('edit-service-duration').value = svc.duracionMinutos;
      document.getElementById('edit-service-status').value   = svc.estado;
      // limpiar checks anteriores
      document.querySelectorAll('#editServiceModal input[name="editVehicleType"]').forEach(cb => cb.checked = false);
      Object.entries(svc.preciosPorTipo || {}).forEach(([tipo, precio]) => {
        const cb   = document.querySelector(`#editServiceModal input[name="editVehicleType"][value="${tipo}"]`);
        const inp  = document.getElementById(`edit-price-${tipo}`);
        if (cb) { cb.checked = true; inp.value = precio; }
      });
      showModal(editModal);
    }
    if (delA) {
      deleteInfoDiv.innerHTML = `<strong>Servicio:</strong> ${svc.nombre}`;
      showModal(deleteModal);
    }
  });

  // Guardar cambios
  saveEditBtn.addEventListener('click', async e => {
    e.preventDefault();
    const seleccionados = Array.from(document.querySelectorAll('#editServiceModal input[name="editVehicleType"]:checked'));
    const preciosPorTipo = {};
    seleccionados.forEach(cb => {
      const tipo = cb.value;
      const inp  = document.getElementById(`edit-price-${tipo}`);
      preciosPorTipo[tipo] = parseFloat(inp.value) || 0;
    });

    const payload = {
      nombre:          editName.value.trim(),
      descripcion:     editDesc.value.trim(),
      duracionMinutos: parseInt(editDuration.value, 10),
      estado:          editStatus.value,
      preciosPorTipo:  preciosPorTipo
    };

    try {
      const resp = await fetch(`/admin/servicios/actualizarServicio/${selectedId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload)
      });
      if (!resp.ok) throw new Error('Error actualizando servicio');
      hideModal(editModal);
      showAlert('success');
      await loadServices();
    } catch (err) {
      console.error(err);
      showAlert('danger');
    }
  });

  // Eliminar servicio
  confirmDeleteBtn.addEventListener('click', async e => {
    e.preventDefault();
    try {
      const resp = await fetch(`/admin/servicios/eliminarServicio/${selectedId}`, {
        method: 'DELETE',
        credentials: 'include'
      });
      if (!resp.ok) throw new Error('Error eliminando servicio');
      hideModal(deleteModal);
      showAlert('success');
      await loadServices();
    } catch (err) {
      console.error(err);
      showAlert('danger');
    }
  });

  // Carga inicial
  loadServices();
});
