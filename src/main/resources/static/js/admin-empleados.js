// static/js/admin-empleados.js

document.addEventListener('DOMContentLoaded', () => {
  // Referencias al DOM
  const openBtn           = document.getElementById('openAddEmployeeModal');
  const addModal          = document.getElementById('addEmployeeModal');
  const closeAddBtn       = document.getElementById('closeModal');
  const cancelAddBtn      = document.getElementById('cancelBtn');
  const addForm           = document.getElementById('employeeForm');

  const editModal         = document.getElementById('editEmployeeModal');
  const closeEditBtn      = document.getElementById('closeEditModal');
  const cancelEditBtn     = document.getElementById('cancelEditBtn');
  const editForm          = document.getElementById('editEmployeeForm');
  const editNombre        = document.getElementById('editNombre');
  const editApellido      = document.getElementById('editApellido');
  const editEmail         = document.getElementById('editEmail');
  const editPassword      = document.getElementById('editpassword');
  const editCedula        = document.getElementById('editcedula');
  const editTelefono      = document.getElementById('editTelefono');
  const editRol           = document.getElementById('editRolEmpleado');

  const deleteModal       = document.getElementById('deleteEmployeeModal');
  const closeDeleteBtn    = document.getElementById('closeDeleteModal');
  const cancelDeleteBtn   = document.getElementById('cancelDeleteBtn');
  const confirmDeleteBtn  = document.getElementById('confirmDeleteBtn');

  const tbody             = document.getElementById('empleadosTbody');
  const paginationEl      = document.getElementById('empleadosPagination');
  const totalEl           = document.getElementById('totalEmployees');

  let currentPage         = 0;
  const pageSize          = 5;
  let selectedId          = null;

  // Mostrar/ocultar modales
  function showModal(m) { m.style.display = 'flex'; }
  function hideModal(m) { m.style.display = 'none'; }

  // Ocultar todos los modales al inicio
  [addModal, editModal, deleteModal].forEach(m => hideModal(m));

  // Eventos modal Agregar
  openBtn.addEventListener('click', () => showModal(addModal));
  closeAddBtn.addEventListener('click', () => hideModal(addModal));
  cancelAddBtn.addEventListener('click', () => hideModal(addModal));

  // Eventos modal Editar
  closeEditBtn.addEventListener('click', () => hideModal(editModal));
  cancelEditBtn.addEventListener('click', () => hideModal(editModal));

  // Eventos modal Eliminar
  closeDeleteBtn.addEventListener('click', () => hideModal(deleteModal));
  cancelDeleteBtn.addEventListener('click', () => hideModal(deleteModal));

  // Cerrar modales al hacer clic fuera
  window.addEventListener('click', e => {
    if (e.target === addModal) hideModal(addModal);
    if (e.target === editModal) hideModal(editModal);
    if (e.target === deleteModal) hideModal(deleteModal);
  });

  // Carga y render de empleados
  async function loadEmpleados(page = 0) {
    currentPage = page;
    try {
      const resp = await fetch(`/admin/empleados?page=${page}&size=${pageSize}`, { credentials: 'include' });
      if (!resp.ok) throw new Error('Error al cargar empleados');
      const data = await resp.json();
      renderTable(data.content);
      renderPagination(data.totalPages, page);
      if (totalEl) totalEl.textContent = data.totalElements;
    } catch (err) {
      console.error('Error cargando empleados:', err);
      showAlert('saveEmployeeAlert', 'Error al cargar empleados');
    }
  }

  function renderTable(emps) {
    tbody.innerHTML = '';
    emps.forEach(emp => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${emp.id}</td>
        <td>
          <div class="employee-info">
            <div class="employee-avatar">${emp.nombre.charAt(0)}${emp.apellido.charAt(0)}</div>
            <div>
              <div class="employee-name">${emp.nombre} ${emp.apellido}</div>
              <div class="employee-email">${emp.email}</div>
            </div>
          </div>
        </td>
        <td>${emp.roles.includes('EMPLOYEE') ? 'Employee' : 'Admin'}</td>
        <td>${emp.cedula}</td>
        <td>${emp.telefono}</td>
        <td>${new Date(emp.fechaRegistro).toLocaleDateString()}</td>
        <td>
          <div class="table-actions">
            <a href="#" class="table-action" title="Editar"><i class="fas fa-edit"></i></a>
            <a href="#" class="table-action" title="Eliminar"><i class="fas fa-trash"></i></a>
          </div>
        </td>`;
      tbody.appendChild(tr);
    });
    setupRowActions();
  }

  function renderPagination(totalPages, current) {
    paginationEl.innerHTML = '';
    const prevBtn = document.createElement('button');
    prevBtn.className = 'pagination-btn';
    prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prevBtn.disabled = current === 0;
    prevBtn.onclick = () => loadEmpleados(current - 1);
    paginationEl.appendChild(prevBtn);

    for (let i = 0; i < totalPages; i++) {
      const btn = document.createElement('button');
      btn.className = `pagination-btn${i === current ? ' active' : ''}`;
      btn.textContent = i + 1;
      btn.onclick = () => loadEmpleados(i);
      paginationEl.appendChild(btn);
    }

    const nextBtn = document.createElement('button');
    nextBtn.className = 'pagination-btn';
    nextBtn.innerHTML = '<i class="fas fa-chevron-right"></i>';
    nextBtn.disabled = current === totalPages - 1;
    nextBtn.onclick = () => loadEmpleados(current + 1);
    paginationEl.appendChild(nextBtn);
  }

  // Agregar empleado
  addForm.addEventListener('submit', async e => {
    e.preventDefault();
    const data = {
      nombre:   addForm.nombre.value.trim(),
      apellido: addForm.apellido.value.trim(),
      email:    addForm.email.value.trim(),
      password: addForm.password.value,
      cedula:   addForm.cedula.value.trim(),
      telefono: addForm.telefono.value.trim(),
      roles:    ['EMPLOYEE']
    };
    try {
      const resp = await fetch('/admin/empleados/crear', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(data)
      });
      if (resp.ok) {
        hideModal(addModal);
        showAlert('saveEmployeeAlert', 'Empleado agregado correctamente');
        loadEmpleados(currentPage);
        addForm.reset();
      } else {
        const err = await resp.json();
        showAlert('saveEmployeeAlert', err.message || 'Error al crear empleado');
      }
    } catch (err) {
      console.error('Error de red al crear empleado:', err);
      showAlert('saveEmployeeAlert', 'Error de red al crear empleado');
    }
  });

  // Editar empleado
  editForm.addEventListener('submit', async e => {
    e.preventDefault();
    const data = {
      nombre:   editNombre.value.trim(),
      apellido: editApellido.value.trim(),
      email:    editEmail.value.trim(),
      password: editPassword.value,
      cedula:   editCedula.value.trim(),
      telefono: editTelefono.value.trim(),
      roles:    [editRol.value]
    };
    try {
      const resp = await fetch(`/admin/empleados/actualizar/${selectedId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(data)
      });
      if (resp.ok) {
        hideModal(editModal);
        showAlert('saveEmployeeAlert', 'Empleado actualizado correctamente');
        loadEmpleados(currentPage);
      } else {
        const err = await resp.json();
        showAlert('saveEmployeeAlert', err.message || 'Error al actualizar empleado');
      }
    } catch (err) {
      console.error('Error de red al actualizar empleado:', err);
      showAlert('saveEmployeeAlert', 'Error de red al actualizar empleado');
    }
  });

  // Eliminar empleado
  confirmDeleteBtn.addEventListener('click', async () => {
    try {
      const resp = await fetch(`/admin/empleados/eliminar/${selectedId}`, {
        method: 'DELETE',
        credentials: 'include'
      });
      if (resp.ok) {
        hideModal(deleteModal);
        showAlert('deleteEmployeeAlert', 'Empleado eliminado correctamente');
        loadEmpleados(currentPage);
      } else {
        showAlert('deleteEmployeeAlert', 'Error al eliminar empleado');
      }
    } catch (err) {
      console.error('Error de red al eliminar empleado:', err);
      showAlert('deleteEmployeeAlert', 'Error de red al eliminar empleado');
    }
  });

  // Asignar eventos a los botones de cada fila
  function setupRowActions() {
    document.querySelectorAll('#empleadosTbody tr').forEach(tr => {
      const id = tr.children[0].textContent.trim();
      const editBtn = tr.querySelector('i.fa-edit')?.parentElement;
      const deleteBtn = tr.querySelector('i.fa-trash')?.parentElement;
      if (editBtn) {
        editBtn.addEventListener('click', e => {
          e.preventDefault();
          selectedId = id;
          const nameParts = tr.querySelector('.employee-name').textContent.split(' ');
          editNombre.value   = nameParts[0] || '';
          editApellido.value = nameParts[1] || '';
          editEmail.value    = tr.querySelector('.employee-email').textContent;
          editPassword.value = '';
          editCedula.value   = tr.children[3].textContent;
          editTelefono.value = tr.children[4].textContent;
          editRol.value      = tr.children[2].textContent.toUpperCase();
          showModal(editModal);
        });
      }
      if (deleteBtn) {
        deleteBtn.addEventListener('click', e => {
          e.preventDefault();
          selectedId = id;
          const name = tr.querySelector('.employee-name').textContent;
          deleteModal.querySelector('.delete-confirmation h3').textContent =
            `¿Estás seguro de eliminar este empleado ${name} (#${id})?`;
          showModal(deleteModal);
        });
      }
    });
  }

  // Mostrar alertas
  function showAlert(alertId, message = null) {
    const alertEl = document.getElementById(alertId);
    if (message) alertEl.querySelector('.alert-text').textContent = message;
    alertEl.classList.add('show');
    setTimeout(() => alertEl.classList.remove('show'), 3000);
  }

  // Primera carga
  loadEmpleados(0);
});
