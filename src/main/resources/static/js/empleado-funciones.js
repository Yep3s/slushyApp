// employee-funciones.js

// Base dinámica según el dominio actual
const API_BASE        = `${window.location.origin}/empleado/reservas`;
const API_PENDIENTES  = `${API_BASE}/pendientes`;
const API_CONFIRMADAS = `${API_BASE}/confirmadas`;
const API_EN_PROCESO  = `${API_BASE}/enProceso`;

let activeReservationId = null;
let activeDurationMs    = 0;
let autoProgressInterval;

// Helpers para persistir/reanudar inicio del timer
function getStoredStart(id) {
  const v = localStorage.getItem(`activeStart_${id}`);
  return v ? parseInt(v, 10) : null;
}
function storeStart(id, ts) {
  localStorage.setItem(`activeStart_${id}`, ts);
}

// Cambia a la pestaña “Servicio Activo” y detiene el timer anterior
function redirectToActiveContent() {
  clearInterval(autoProgressInterval);
  document.querySelectorAll('.employee-tab').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.tab-content').forEach(tc => tc.classList.remove('active'));
  document.querySelector('.employee-tab[data-tab="active"]').classList.add('active');
  document.getElementById('active-content').classList.add('active');
}

// ── Carga reservas PENDIENTES ─────────────────────────────────────────────────
async function loadPendientes() {
  try {
    const res = await fetch(API_PENDIENTES, { credentials: 'include' });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const pendientes = await res.json();

    document.getElementById('pending-count').textContent = pendientes.length;
    document.getElementById('queue-pending-list').innerHTML = pendientes.map(r => `
      <div class="queue-item" data-id="${r.id}"
           data-placa="${r.placa}"
           data-tipo="${r.tipoVehiculo}"
           data-usuario="${r.usuarioEmail}"
           data-servicio="${r.servicioNombre}"
           data-hora="${new Date(r.fechaInicio).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}"
           data-notas="${r.observaciones||''}"
           data-duracion="${r.duracionMinutos}">
        <div class="queue-item-header">
          <h4 class="queue-item-title">${r.placa}</h4>
          <span class="queue-item-time">${
            new Date(r.fechaInicio).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})
          }</span>
        </div>
        <div class="queue-item-details">
          <div class="queue-item-detail"><i class="fas fa-user"></i><span>${r.usuarioEmail}</span></div>
          <div class="queue-item-detail"><i class="fas fa-car"></i><span>${r.tipoVehiculo} • ${r.placa}</span></div>
          <div class="queue-item-detail"><i class="fas fa-list-check"></i><span>${r.servicioNombre}</span></div>
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
      await fetch(`${API_BASE}/confirmar/${id}`, { method:'PUT', credentials:'include' });
      await loadPendientes();
      await loadConfirmadas();
    };
  });
}

// ── Carga reservas CONFIRMADAS ────────────────────────────────────────────────
async function loadConfirmadas() {
  try {
    const res = await fetch(API_CONFIRMADAS, { credentials: 'include' });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const confirmadas = await res.json();

    document.getElementById('confirmed-count').textContent = confirmadas.length;
    document.getElementById('queue-confirmed-list').innerHTML = confirmadas.map(r => `
      <div class="queue-item" data-id="${r.id}"
           data-placa="${r.placa}"
           data-tipo="${r.tipoVehiculo}"
           data-usuario="${r.usuarioEmail}"
           data-servicio="${r.servicioNombre}"
           data-hora="${new Date(r.fechaInicio).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}"
           data-notas="${r.observaciones||''}"
           data-duracion="${r.duracionMinutos}">
        <div class="queue-item-header">
          <h4 class="queue-item-title">${r.placa}</h4>
          <span class="queue-item-time">${
            new Date(r.fechaInicio).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})
          }</span>
        </div>
        <div class="queue-item-details">
          <div class="queue-item-detail"><i class="fas fa-user"></i><span>${r.usuarioEmail}</span></div>
          <div class="queue-item-detail"><i class="fas fa-car"></i><span>${r.tipoVehiculo} • ${r.placa}</span></div>
          <div class="queue-item-detail"><i class="fas fa-list-check"></i><span>${r.servicioNombre}</span></div>
          <div class="queue-item-detail"><i class="fas fa-check-circle"></i><span>Confirmada</span></div>
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
      const id = btn.closest('.queue-item').dataset.id;
      const res = await fetch(`${API_BASE}/iniciar/${id}`, { method:'PUT', credentials:'include' });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const r = await res.json();

      activeReservationId = id;
      activeDurationMs    = r.duracionMinutos * 60 * 1000;

      await loadConfirmadas();
      await loadEnProceso();

      fillActiveService(r);
      redirectToActiveContent();
    };
  });
}

// ── Carga reservas EN_PROCESO (solo lista, NO redirige) ──────────────────────
async function loadEnProceso() {
  try {
    const res = await fetch(API_EN_PROCESO, { credentials: 'include' });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const enProceso = await res.json();

    // pinta contador y lista en la pestaña “Cola de Vehículos”
    document.getElementById('inprocess-count').textContent = enProceso.length;
    document.getElementById('queue-inprocess-list').innerHTML = enProceso.map(r => `
      <div class="queue-item" data-id="${r.id}">
        <div class="queue-item-header">
          <h4 class="queue-item-title">${r.placa}</h4>
          <span class="queue-item-time">${
            new Date(r.fechaInicio).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})
          }</span>
        </div>
        <div class="queue-item-details">
          <div class="queue-item-detail"><i class="fas fa-user"></i><span>${r.usuarioEmail}</span></div>
          <div class="queue-item-detail"><i class="fas fa-list-check"></i><span>${r.servicioNombre}</span></div>
          <div class="queue-item-detail"><i class="fas fa-tasks"></i><span>Progreso: ${r.progreso}%</span></div>
        </div>
        <div class="queue-item-actions">
          <button class="btn btn-primary resume-btn">Retomar Servicio</button>
        </div>
      </div>
    `).join('');

    attachResumeListeners();
    attachDetailsListeners();

  } catch (err) {
    console.error('❌ loadEnProceso()', err);
  }
}

function attachResumeListeners() {
  document.querySelectorAll('.resume-btn').forEach(btn => {
    btn.onclick = async () => {
      const id = btn.closest('.queue-item').dataset.id;
      // rescata la reserva EN_PROCESO
      const res  = await fetch(API_EN_PROCESO, { credentials:'include' });
      const list = await res.json();
      const r    = list.find(x => x.id === id);
      if (!r) return;

      // carga “Servicio Activo”
      activeReservationId = r.id;
      activeDurationMs    = r.duracionMinutos * 60 * 1000;
      fillActiveService(r);

      // si ya hay progreso, recalcula y reanuda
      if (r.progreso > 0) {
        const elapsed = (r.progreso/100)*activeDurationMs;
        const t0 = Date.now() - elapsed;
        storeStart(r.id, t0);
        startAutoProgress(t0);
      }

      redirectToActiveContent();
    };
  });
}

// ── Progreso automático ───────────────────────────────────────────────────────
function startAutoProgress(startTime) {
  clearInterval(autoProgressInterval);
  const bar = document.querySelector('.progress-bar');
  bar.style.transition = 'width 0.5s ease';

  autoProgressInterval = setInterval(() => {
    const elapsed = Date.now() - startTime;
    const pct     = Math.min(100, Math.round(elapsed / activeDurationMs * 100));

    bar.style.width   = pct + '%';
    document.querySelector('.progress-value').textContent = pct + '%';

    const remMs = Math.max(0, activeDurationMs - elapsed);
    const mins  = Math.floor(remMs / 60000);
    const secs  = String(Math.floor((remMs % 60000) / 1000)).padStart(2,'0');
    document.getElementById('time-remaining').textContent = `${mins}m ${secs}s`;

    // auto-completa pasos...
    const steps = Array.from(document.querySelectorAll('.step-item'));
    const inc   = 100 / steps.length;
    steps.forEach((step, i) => {
      if (pct >= inc * (i+1) && !step.classList.contains('completed')) {
        step.classList.add('completed');
        step.querySelector('.step-actions .btn-primary')?.replaceWith(
          `<span>Completado a las ${new Date().toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}</span>`
        );
      }
    });

    if (pct >= 100) {
      clearInterval(autoProgressInterval);
      document.getElementById('confirmFinishBtn').disabled = false;
    }
  }, 1000);
}

// ── Rellena “Servicio Activo” ───────────────────────────────────────────────
function fillActiveService(r) {
  document.getElementById('active-title').textContent        = `${r.placa} • ${r.servicioNombre}`;
  document.getElementById('active-status').textContent       = 'En Proceso';
  document.getElementById('active-client').textContent       = r.usuarioEmail;
  document.getElementById('active-vehicle').textContent      = `${r.tipoVehiculo} • ${r.placa}`;
  document.getElementById('active-service-name').textContent = r.servicioNombre;
  document.getElementById('start-time').textContent          = new Date().toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'});
  document.getElementById('total-duration').textContent      = `${r.duracionMinutos} minutos`;
  document.getElementById('confirmFinishBtn').disabled       = true;
  bindStepListeners();
}

// ── Pasos manuales ──────────────────────────────────────────────────────────
function bindStepListeners() {
  const steps     = Array.from(document.querySelectorAll('.step-item'));
  const increment = Math.round(100 / steps.length);

  steps.forEach((step, idx) => {
    const btn = step.querySelector('.step-actions .btn-primary');
    if (!btn) return;
    btn.onclick = async () => {
      if (idx === 0) {
        const t0 = Date.now();
        storeStart(activeReservationId, t0);
        startAutoProgress(t0);
      }
      step.classList.add('completed');
      btn.replaceWith(`<span>Completado a las ${new Date().toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}</span>`);

      let curr = parseInt(document.querySelector('.progress-value').textContent);
      const next = Math.min(100, curr + increment);
      document.querySelector('.progress-bar').style.width   = `${next}%`;
      document.querySelector('.progress-value').textContent = `${next}%`;

      const newElapsed   = (next/100) * activeDurationMs;
      const newStartTime = Date.now() - newElapsed;
      storeStart(activeReservationId, newStartTime);
      clearInterval(autoProgressInterval);
      startAutoProgress(newStartTime);

      await fetch(`${API_BASE}/progreso/${activeReservationId}?progreso=${next}`, {
        method:'PUT', credentials:'include'
      });

      if (idx === steps.length - 2) steps[steps.length - 1].classList.add('completed');
      if (steps.every(s => s.classList.contains('completed'))) {
        document.getElementById('confirmFinishBtn').disabled = false;
      }
    };
  });
}

// ── Modal de detalles ─────────────────────────────────────────────────────────
function attachDetailsListeners() {
  document.querySelectorAll('.details-btn').forEach(btn => {
    btn.onclick = () => {
      const it = btn.closest('.queue-item');
      showDetailsModal(
        it.dataset.placa,
        it.dataset.usuario,
        `${it.dataset.tipo} • ${it.dataset.placa}`,
        it.dataset.servicio,
        it.dataset.hora,
        it.dataset.notas
      );
    };
  });
}

// ── Finalizar servicio ────────────────────────────────────────────────────────
document.getElementById('confirmFinishBtn').addEventListener('click', async () => {
  clearInterval(autoProgressInterval);
  const res = await fetch(`${API_BASE}/completar/${activeReservationId}`, {
    method:'PUT', credentials:'include'
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);

  // Fuerza UI al 100%
  document.querySelector('.progress-bar').style.width   = '100%';
  document.querySelector('.progress-value').textContent = '100%';
  document.getElementById('time-remaining').textContent = '0m 00s';

  closeModal('finishConfirmationModal');
  document.querySelector('.employee-tab[data-tab="queue"]').click();
  await loadPendientes();
  await loadConfirmadas();
});

// ── Inicialización ────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  loadPendientes();
  loadConfirmadas();
  loadEnProceso();
  setInterval(loadPendientes,   5000);
  setInterval(loadConfirmadas,  5000);
  setInterval(loadEnProceso,    5000);
});
