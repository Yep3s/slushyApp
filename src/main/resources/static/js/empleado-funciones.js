// employee-funciones.js

// 1. Base dinámica: siempre apunta al mismo dominio + protocolo que
//    tengas en la barra de direcciones (http:// o https://)
const API_BASE       = `${window.location.origin}/empleado/reservas`;
const API_PENDIENTES = `${API_BASE}/pendientes`;
const API_CONFIRMADAS= `${API_BASE}/confirmadas`;
const API_EN_PROCESO = `${API_BASE}/enProceso`;

// ── PENDIENTES ───────────────────────────────────────────────────────────────
async function loadPendientes() {
  try {
    const res = await fetch(API_PENDIENTES, { credentials: 'include' });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const pendientes = await res.json();

    const lista    = document.getElementById('queue-pending-list');
    const contador = document.getElementById('pending-count');
    contador.textContent = pendientes.length;

    lista.innerHTML = pendientes.map(r => `
      <div class="queue-item" data-id="${r.id}">
        <div class="queue-item-header">
          <h4 class="queue-item-title">${r.placa}</h4>
          <span class="queue-item-time">
            ${new Date(r.fechaInicio)
                .toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}
          </span>
        </div>
        <div class="queue-item-details">
          <div class="queue-item-detail">
            <i class="fas fa-user queue-item-icon"></i>
            <span>${r.usuarioEmail}</span>
          </div>
          <div class="queue-item-detail">
            <i class="fas fa-car queue-item-icon"></i>
            <span>${r.tipoVehiculo} • ${r.placa}</span>
          </div>
          <div class="queue-item-detail">
            <i class="fas fa-list-check queue-item-icon"></i>
            <span>${r.servicioNombre}</span>
          </div>
        </div>
        <div class="queue-item-actions">
          <button class="btn btn-primary confirm-btn">Confirmar</button>
          <button class="btn btn-outline details-btn">Ver Detalles</button>
        </div>
      </div>
    `).join('');

    attachPendingListeners();
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

// ── CONFIRMADAS ─────────────────────────────────────────────────────────────
async function loadConfirmadas() {
  try {
    const res = await fetch(API_CONFIRMADAS, { credentials: 'include' });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const confirmadas = await res.json();

    const lista    = document.getElementById('queue-confirmed-list');
    const contador = document.getElementById('confirmed-count');
    contador.textContent = confirmadas.length;

    lista.innerHTML = confirmadas.map(r => `
      <div class="queue-item" data-id="${r.id}">
        <div class="queue-item-header">
          <h4 class="queue-item-title">${r.placa}</h4>
          <span class="queue-item-time">
            ${new Date(r.fechaInicio)
                .toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}
          </span>
        </div>
        <div class="queue-item-details">
          <div class="queue-item-detail">
            <i class="fas fa-user queue-item-icon"></i>
            <span>${r.usuarioEmail}</span>
          </div>
          <div class="queue-item-detail">
            <i class="fas fa-car queue-item-icon"></i>
            <span>${r.tipoVehiculo} • ${r.placa}</span>
          </div>
          <div class="queue-item-detail">
            <i class="fas fa-list-check queue-item-icon"></i>
            <span>${r.servicioNombre}</span>
          </div>
          <div class="queue-item-detail">
            <i class="fas fa-check-circle queue-item-icon"></i>
            <span>Confirmada</span>
          </div>
        </div>
        <div class="queue-item-actions">
          <button class="btn btn-primary start-btn">Iniciar Servicio</button>
          <button class="btn btn-outline details-btn">Ver Detalles</button>
        </div>
      </div>
    `).join('');

    attachConfirmedListeners();
  } catch (err) {
    console.error('❌ loadConfirmadas()', err);
  }
}

function attachConfirmedListeners() {
  document.querySelectorAll('.start-btn').forEach(btn => {
    btn.onclick = async () => {
      const id = btn.closest('.queue-item').dataset.id;
      await fetch(`${API_BASE}/iniciar/${id}`, {
        method: 'PUT',
        credentials: 'include'
      });
      await loadConfirmadas();
      await loadEnProceso();
    };
  });
}

// ── EN_PROCESO ──────────────────────────────────────────────────────────────
async function loadEnProceso() {
  try {
    const res = await fetch(API_EN_PROCESO, { credentials: 'include' });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const enProceso = await res.json();

    const lista    = document.getElementById('queue-active-list');
    const badge    = document.getElementById('active-count');
    if (badge) badge.textContent = enProceso.length;

    lista.innerHTML = enProceso.map(r => `
      <div class="queue-item" data-id="${r.id}">
        <div class="queue-item-header">
          <h4 class="queue-item-title">${r.placa}</h4>
          <span class="queue-item-time">
            ${new Date(r.fechaInicio)
                .toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}
          </span>
        </div>
        <div class="queue-item-details">
          <div class="queue-item-detail">
            <i class="fas fa-user queue-item-icon"></i>
            <span>${r.usuarioEmail}</span>
          </div>
          <div class="queue-item-detail">
            <i class="fas fa-list-check queue-item-icon"></i>
            <span>${r.servicioNombre}</span>
          </div>
          <div class="queue-item-detail">
            <i class="fas fa-tasks queue-item-icon"></i>
            <span>Progreso: ${r.progreso}%</span>
          </div>
        </div>
        <div class="queue-item-actions">
          <button class="btn btn-success complete-btn">Completar Servicio</button>
          <button class="btn btn-outline details-btn">Ver Detalles</button>
        </div>
      </div>
    `).join('');

    attachProcessingListeners();
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

// ── INICIALIZACIÓN Y REFRESCO ───────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  loadPendientes();
  loadConfirmadas();
  loadEnProceso();

  setInterval(loadPendientes,   5000);
  setInterval(loadConfirmadas,  5000);
  setInterval(loadEnProceso,    5000);
});
