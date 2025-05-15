// static/js/admin-servicios.js

document.addEventListener('DOMContentLoaded', function() {
  // ====== GENERIC ADMIN BEHAVIOR ======

  // 1. Menú responsive
  const menuToggle = document.getElementById('menuToggle');
  menuToggle.addEventListener('click', () => {
    document.getElementById('adminSidebar').classList.toggle('active');
  });

  // 2. Pestañas
  const tabBtns = document.querySelectorAll('.tab-btn');
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
  function showModal(modal)   { modal.classList.add('active'); }
  function hideModal(modal)   { modal.classList.remove('active'); }

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

  const serviceName      = document.getElementById('service-name');
  const serviceDuration  = document.getElementById('service-duration');
  const servicePrice     = document.getElementById('service-price');
  const serviceStatus    = document.getElementById('service-status');
  const serviceDesc      = document.getElementById('service-description');

  const editName         = document.getElementById('edit-service-name');
  const editDuration     = document.getElementById('edit-service-duration');
  const editPrice        = document.getElementById('edit-service-price');
  const editStatus       = document.getElementById('edit-service-status');
  const editDesc         = document.getElementById('edit-service-description');

  const deleteInfoDiv    = deleteModal.querySelector('.service-to-delete');
  const tbody            = document.querySelector('#servicesTable tbody');

  let allServices = [];
  let selectedId  = null;

  function clearAddForm() {
    serviceName.value     = '';
    serviceDuration.value = '';
    servicePrice.value    = '';
    serviceStatus.value   = 'ACTIVO';
    serviceDesc.value     = '';
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
      const tr = document.createElement('tr');
      tr.dataset.id = s.id;
      tr.innerHTML = `
        <td>${s.id}</td>
        <td>${s.nombre}</td>
        <td>${s.descripcion}</td>
        <td>${s.duracionMinutos} min</td>
        <td>$${s.precio.toFixed(2)}</td>
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

  openAddBtn.addEventListener('click', () => {
    clearAddForm();
    showModal(addModal);
  });

  saveAddBtn.addEventListener('click', async e => {
    e.preventDefault();
    const payload = {
      nombre:          serviceName.value.trim(),
      descripcion:     serviceDesc.value.trim(),
      precio:          parseFloat(servicePrice.value),
      duracionMinutos: parseInt(serviceDuration.value, 10),
      estado:          serviceStatus.value
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

  tbody.addEventListener('click', e => {
    const tr    = e.target.closest('tr');
    if (!tr) return;
    selectedId = tr.dataset.id;
    const svc = allServices.find(s => s.id === selectedId);
    const editA = e.target.closest('a[title="Editar"]');
    const delA  = e.target.closest('a[title="Eliminar"]');
    if (editA) {
      editName.value     = svc.nombre;
      editDesc.value     = svc.descripcion;
      editDuration.value = svc.duracionMinutos;
      editPrice.value    = svc.precio;
      editStatus.value   = svc.estado;
      showModal(editModal);
    }
    if (delA) {
      deleteInfoDiv.innerHTML = `<strong>Servicio:</strong> ${svc.nombre}`;
      showModal(deleteModal);
    }
  });

  saveEditBtn.addEventListener('click', async e => {
    e.preventDefault();
    const payload = {
      nombre:          editName.value.trim(),
      descripcion:     editDesc.value.trim(),
      precio:          parseFloat(editPrice.value),
      duracionMinutos: parseInt(editDuration.value, 10),
      estado:          editStatus.value
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

  // Initial load
  loadServices();
});
