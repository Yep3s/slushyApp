// static/js/admin-empleados.js

// Referencias al DOM
const openBtn      = document.getElementById('openAddEmployeeModal');
const modal        = document.getElementById('addEmployeeModal');
const closeBtn     = document.getElementById('closeModal');
const cancelBtn    = document.getElementById('cancelBtn');
const form         = document.getElementById('employeeForm');
const tbody        = document.getElementById('empleadosTbody');
const paginationEl = document.getElementById('empleadosPagination');

// Variables de paginación
let currentPage = 0;
const pageSize   = 5;

// Funciones modal
function showModal() {
  modal.style.display = 'flex';
}

function hideModal() {
  modal.style.display = 'none';
  form.reset();
}

// Inicialización: modal oculto y centrado por CSS
modal.style.display = 'none';
// Asegúrate en CSS de tener:
// .modal { display: flex; align-items: center; justify-content: center; }

// Eventos modal
openBtn.addEventListener('click', showModal);
closeBtn.addEventListener('click', hideModal);
cancelBtn.addEventListener('click', hideModal);
window.addEventListener('click', e => {
  if (e.target === modal) hideModal();
});

// Envío del formulario
form.addEventListener('submit', async e => {
  e.preventDefault();
  const data = {
    nombre:   form.nombre.value.trim(),
    apellido: form.apellido.value.trim(),
    email:    form.email.value.trim(),
    password: form.password.value,
    cedula:   form.cedula.value.trim(),
    telefono: form.telefono.value.trim()
  };

  try {
    const resp = await fetch('/admin/empleados/crear', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(data)
    });
    if (!resp.ok) {
      const errorData = await resp.json();
      alert(errorData.message || 'Error al crear empleado');
      return;
    }

    // Una vez confirmado en el servidor, cerramos modal, mostramos alerta y recargamos tabla
    hideModal();
    showAlert('saveEmployeeAlert', 'Empleado agregado correctamente');
    await loadEmpleados(currentPage);

  } catch (err) {
    console.error('Error de red al crear empleado:', err);
    alert('Error de red al crear empleado');
  }
});

// Carga y render de empleados
async function loadEmpleados(page = 0) {
  currentPage = page;
  try {
    const resp = await fetch(`/admin/empleados?page=${page}&size=${pageSize}`, {
      credentials: 'include'
    });
    if (!resp.ok) throw new Error('Error al cargar empleados');
    const data = await resp.json();
    renderTable(data.content);
    renderPagination(data.totalPages, page);
  } catch (err) {
    console.error('Error cargando empleados:', err);
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
      <td>${emp.roles && emp.roles.includes('EMPLOYEE') ? 'Employee' : 'Admin'}</td>
      <td>${emp.telefono}</td>
      <td>${new Date(emp.fechaRegistro).toLocaleDateString()}</td>
      <td>
        <div class="table-actions">
          <a href="#" class="table-action"><i class="fas fa-eye"></i></a>
          <a href="#" class="table-action"><i class="fas fa-edit"></i></a>
          <a href="#" class="table-action"><i class="fas fa-trash"></i></a>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });
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

// Función para mostrar alertas
function showAlert(alertId, message = null) {
  const alert = document.getElementById(alertId);
  if (message) {
    alert.querySelector('.alert-text').textContent = message;
  }
  alert.classList.add('show');
  setTimeout(() => alert.classList.remove('show'), 3000);
}

// Inicializar tabla al cargar la página
document.addEventListener('DOMContentLoaded', () => loadEmpleados(0));
