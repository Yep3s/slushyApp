// employee-funciones.js

// Base dinámica según el dominio actual
const API_BASE        = `${window.location.origin}/empleado/reservas`;
const API_PENDIENTES  = `${API_BASE}/pendientes`;
const API_CONFIRMADAS = `${API_BASE}/confirmadas`;
const API_EN_PROCESO  = `${API_BASE}/enProceso`;

let activeReservationId = null;
let activeStartTime     = 0;
let activeDurationMs    = 0;
let autoProgressInterval;

// ── PENDIENTES ───────────────────────────────────────────────────────────────
async function loadPendientes() {
  try {
    const res = await fetch(API_PENDIENTES, { credentials: 'include' });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const pendientes = await res.json();

    document.getElementById('pending-count').textContent = pendientes.length;
    document.getElementById('queue-pending-list').innerHTML = pendientes.map(r => `
      <div class="queue-item"
           data-id="${r.id}"
           data-placa="${r.placa}"
           data-tipo="${r.tipoVehiculo}"
           data-usuario="${r.usuarioEmail}"
           data-servicio="${r.servicioNombre}"
           data-hora="${new Date(r.fechaInicio).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}"
           data-notas="${r.observaciones || ''}"
           data-duracion="${r.duracionMinutos}">
        <div class="queue-item-header">
          <h4 class="queue-item-title">${r.placa}</h4>
          <span class="queue-item-time">${new Date(r.fechaInicio)
              .toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}</span>
        </div>
        <div class="queue-item-details">
          <div class="queue-item-detail">
            <i class="fas fa-user"></i><span>${r.usuarioEmail}</span>
          </div>
          <div class="queue-item-detail">
            <i class="fas fa-car"></i><span>${r.tipoVehiculo} • ${r.placa}</span>
          </div>
          <div class="queue-item-detail">
            <i class="fas fa-list-check"></i><span>${r.servicioNombre}</span>
          </div>
        </div>
        <div class="queue-item-actions">
          <button class="btn btn-primary confirm-btn">Confirmar</button>
          <button class="btn btn-outline details-btn">Ver Detalles</button>
        </div>
      </div>
    `).join('');

    attachPendingListeners();
    attachDetailsListeners();
  } catch (err) {
    console.error('❌ loadPendientes()', err);
  }
}

function attachPendingListeners() {
  document.querySelectorAll('.confirm-btn').forEach(btn => {
    btn.onclick = async () => {
      const id = btn.closest('.queue-item').dataset.id;
      await fetch(`${API_BASE}/confirmar/${id}`, {
        method: 'PUT',
        credentials: 'include'
      });
      await loadPendientes();
      await loadConfirmadas();
    };
  });
}

// ── CONFIRMADAS ───────────────────────────────────────────────────────────────
async function loadConfirmadas() {
  try {
    const res = await fetch(API_CONFIRMADAS, { credentials: 'include' });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const confirmadas = await res.json();

    document.getElementById('confirmed-count').textContent = confirmadas.length;
    document.getElementById('queue-confirmed-list').innerHTML = confirmadas.map(r => `
      <div class="queue-item"
           data-id="${r.id}"
           data-placa="${r.placa}"
           data-tipo="${r.tipoVehiculo}"
           data-usuario="${r.usuarioEmail}"
           data-servicio="${r.servicioNombre}"
           data-hora="${new Date(r.fechaInicio).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}"
           data-notas="${r.observaciones || ''}"
           data-duracion="${r.duracionMinutos}">
        <div class="queue-item-header">
          <h4 class="queue-item-title">${r.placa}</h4>
          <span class="queue-item-time">${new Date(r.fechaInicio)
              .toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}</span>
        </div>
        <div class="queue-item-details">
          <div class="queue-item-detail">
            <i class="fas fa-user"></i><span>${r.usuarioEmail}</span>
          </div>
          <div class="queue-item-detail">
            <i class="fas fa-car"></i><span>${r.tipoVehiculo} • ${r.placa}</span>
          </div>
          <div class="queue-item-detail">
            <i class="fas fa-list-check"></i><span>${r.servicioNombre}</span>
          </div>
          <div class="queue-item-detail">
            <i class="fas fa-check-circle"></i><span>Confirmada</span>
          </div>
        </div>
        <div class="queue-item-actions">
          <button class="btn btn-primary start-btn">Iniciar Servicio</button>
          <button class="btn btn-outline details-btn">Ver Detalles</button>
        </div>
      </div>
    `).join('');

    attachConfirmedListeners();
    attachDetailsListeners();
  } catch (err) {
    console.error('❌ loadConfirmadas()', err);
  }
}

function attachConfirmedListeners() {
  document.querySelectorAll('.start-btn').forEach(btn => {
    btn.onclick = async () => {
      const item = btn.closest('.queue-item');
      const id   = item.dataset.id;

      // 1) Cambia a EN_PROCESO y recibe r con duracionMinutos
      const res = await fetch(`${API_BASE}/iniciar/${id}`, {
        method: 'PUT',
        credentials: 'include'
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const r = await res.json();

      // 2) Guarda id y usa FECHA_ACTUAL como inicio
      activeReservationId = id;
      activeStartTime     = Date.now();
      activeDurationMs    = r.duracionMinutos * 60 * 1000;

      // 3) Refresca colas
      await loadConfirmadas();
      await loadEnProceso();

      // 4) Rellena y muestra Servicio Activo
      fillActiveService(r);
      redirectToActiveContent();
    };
  });
}

// ── EN PROCESO ────────────────────────────────────────────────────────────────
async function loadEnProceso() {
  try {
    const res = await fetch(API_EN_PROCESO, { credentials: 'include' });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const enProceso = await res.json();

    document.getElementById('active-count').textContent = enProceso.length;
    document.getElementById('queue-active-list').innerHTML = enProceso.map(r => `
      <div class="queue-item"
           data-id="${r.id}"
           data-placa="${r.placa}"
           data-usuario="${r.usuarioEmail}"
           data-servicio="${r.servicioNombre}"
           data-hora="${new Date(r.fechaInicio).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}"
           data-notas="${r.observaciones || ''}"
           data-progreso="${r.progreso}">
        <div class="queue-item-header">
          <h4 class="queue-item-title">${r.placa}</h4>
          <span class="queue-item-time">${new Date(r.fechaInicio)
              .toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}</span>
        </div>
        <div class="queue-item-details">
          <div class="queue-item-detail">
            <i class="fas fa-user"></i><span>${r.usuarioEmail}</span>
          </div>
          <div class="queue-item-detail">
            <i class="fas fa-list-check"></i><span>${r.servicioNombre}</span>
          </div>
          <div class="queue-item-detail">
            <i class="fas fa-tasks"></i><span>Progreso: ${r.progreso}%</span>
          </div>
        </div>
        <div class="queue-item-actions">
          <button class="btn btn-success complete-btn">Completar Servicio</button>
          <button class="btn btn-outline details-btn">Ver Detalles</button>
        </div>
      </div>
    `).join('');

    attachProcessingListeners();
    attachDetailsListeners();
  } catch (err) {
    console.error('❌ loadEnProceso()', err);
  }
}

function attachProcessingListeners() {
  document.querySelectorAll('.complete-btn').forEach(btn => {
    btn.onclick = async () => {
      const id = btn.closest('.queue-item').dataset.id;
      await fetch(`${API_BASE}/completar/${id}`, {
        method: 'PUT',
        credentials: 'include'
      });
      await loadEnProceso();
    };
  });
}

// ── PROGRESO AUTOMÁTICO ───────────────────────────────────────────────────────
function startAutoProgress() {
  clearInterval(autoProgressInterval);
  autoProgressInterval = setInterval(() => {
    const elapsed = Date.now() - activeStartTime;
    const pct     = Math.min(100, Math.round(elapsed / activeDurationMs * 100));

    document.querySelector('.progress-bar').style.width   = pct + '%';
    document.querySelector('.progress-value').textContent = pct + '%';

    const remMs = Math.max(0, activeDurationMs - elapsed);
    const mins  = Math.floor(remMs / 60000);
    const secs  = String(Math.floor((remMs % 60000) / 1000)).padStart(2, '0');
    document.getElementById('time-remaining').textContent = `${mins}m ${secs}s`;

    if (pct >= 100) clearInterval(autoProgressInterval);
  }, 1000);
}

// ── RELLENA “Servicio Activo” ───────────────────────────────────────────────
function fillActiveService(r) {
  document.getElementById('active-title').textContent        = `${r.placa} • ${r.servicioNombre}`;
  document.getElementById('active-status').textContent       = 'En Proceso';
  document.getElementById('active-client').textContent       = r.usuarioEmail;
  document.getElementById('active-vehicle').textContent      = `${r.tipoVehiculo} • ${r.placa}`;
  document.getElementById('active-service-name').textContent = r.servicioNombre;
  document.getElementById('start-time').textContent          = new Date(activeStartTime)
    .toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  document.getElementById('total-duration').textContent      = `${r.duracionMinutos} minutos`;

  startAutoProgress();
  bindStepListeners();
}

// ── PASOS MANUALES ──────────────────────────────────────────────────────────
function bindStepListeners() {
  const increments = [35, 25, 25, 15]; // suman 100
  document.querySelectorAll('.step-item').forEach((step, idx) => {
    const btn = step.querySelector('.step-actions .btn-primary');
    if (!btn) return;
    btn.onclick = async () => {
      const doneAt = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      step.classList.add('completed');
      btn.replaceWith(`Completado a las ${doneAt}`);

      let current = parseInt(document.querySelector('.progress-value').textContent);
      const next  = Math.min(100, current + increments[idx]);
      document.querySelector('.progress-value').textContent = `${next}%`;
      document.querySelector('.progress-bar').style.width   = `${next}%`;

      await fetch(`${API_BASE}/progreso/${activeReservationId}?progreso=${next}`, {
        method: 'PUT',
        credentials: 'include'
      });
    };
  });
}

// ── DETALLES MODAL ───────────────────────────────────────────────────────────
function attachDetailsListeners() {
  document.querySelectorAll('.details-btn').forEach(btn => {
    btn.onclick = () => {
      const item = btn.closest('.queue-item');
      showDetailsModal(
        item.dataset.placa,
        item.dataset.usuario,
        `${item.dataset.tipo} • ${item.dataset.placa}`,
        item.dataset.servicio,
        item.dataset.hora,
        item.dataset.notas
      );
    };
  });
}

// ── FINALIZAR SERVICIO ────────────────────────────────────────────────────────
document.getElementById('confirmFinishBtn').addEventListener('click', async () => {
  clearInterval(autoProgressInterval);
  await fetch(`${API_BASE}/completar/${activeReservationId}`, {
    method: 'PUT',
    credentials: 'include'
  });
  closeModal('finishConfirmationModal');
  // borra el contenido de active-service para dejarlo en blanco
  document.querySelector('#active-content .active-service').innerHTML = '';
  // redirige a Cola de Vehículos
  document.querySelector('.employee-tab[data-tab="queue"]').click();
});

// ── INICIALIZACIÓN ────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  loadPendientes();
  loadConfirmadas();
  loadEnProceso();
  setInterval(loadPendientes,   5000);
  setInterval(loadConfirmadas,  5000);
  setInterval(loadEnProceso,    5000);
});
