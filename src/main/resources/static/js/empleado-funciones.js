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
  const res = await fetch(API_PENDIENTES, { credentials:'include' });
  if (!res.ok) return console.error('❌ loadPendientes', res.status);
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
  const res = await fetch(API_CONFIRMADAS, { credentials:'include' });
  if (!res.ok) return console.error('❌ loadConfirmadas', res.status);
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
}

function attachConfirmedListeners() {
  document.querySelectorAll('.start-btn').forEach(btn => {
    btn.onclick = async () => {
      const item = btn.closest('.queue-item');
      const id   = item.dataset.id;
      const res  = await fetch(`${API_BASE}/iniciar/${id}`, { method:'PUT', credentials:'include' });
      if (!res.ok) return console.error('❌ iniciar', res.status);
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

// ── Carga reservas EN_PROCESO (solo lista) ────────────────────────────────────
async function loadEnProceso() {
  const res = await fetch(API_EN_PROCESO, { credentials:'include' });
  if (!res.ok) return console.error('❌ loadEnProceso', res.status);
  const enProceso = await res.json();

  // Cola en la pestaña principal
  document.getElementById('inprocess-count').textContent = enProceso.length;
  document.getElementById('queue-inprocess-list').innerHTML = enProceso.map(r => `
    <div class="queue-item" data-id="${r.id}"
         data-placa="${r.placa}"
         data-usuario="${r.usuarioEmail}"
         data-servicio="${r.servicioNombre}"
         data-hora="${new Date(r.fechaInicio).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}">
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
}

function attachResumeListeners() {
  document.querySelectorAll('.resume-btn').forEach(btn => {
    btn.onclick = async () => {
      const id = btn.closest('.queue-item').dataset.id;

      // 1) Trae la lista de enProceso y busca la reserva
      const res  = await fetch(API_EN_PROCESO, { credentials:'include' });
      if (!res.ok) return console.error('❌ loadEnProceso', res.status);
      const list = await res.json();
      const r    = list.find(x => x.id === id);
      if (!r) return;

      // 2) Guarda el contexto
      activeReservationId = r.id;
      activeDurationMs    = r.duracionMinutos * 60 * 1000;

      // 3) Rellena el panel de Servicio Activo
      fillActiveService(r);

      // 4) Calcula o recupera el t0 para el timer
      let t0 = getStoredStart(r.id);
      if (t0 == null) {
        // si nunca arrancó, recalcula desde progreso guardado
        const elapsed = (r.progreso / 100) * activeDurationMs;
        t0 = Date.now() - elapsed;
        storeStart(r.id, t0);
      }
      // fija la hora de inicio en la UI
      document.getElementById('start-time').textContent =
        new Date(t0).toLocaleTimeString([], { hour:'2-digit', minute:'2-digit' });

      // 5) Arranca de nuevo el contador
      if (r.progreso > 0 || t0) {
        startAutoProgress(t0);
      }

      // 6) ¡Y por fin, muestra la pestaña de Servicio Activo!
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

    const rem = Math.max(0, activeDurationMs - elapsed);
    const m   = Math.floor(rem/60000);
    const s   = String(Math.floor((rem%60000)/1000)).padStart(2,'0');
    document.getElementById('time-remaining').textContent = `${m}m ${s}s`;

    // auto-complete pasos sin etiquetas literales
    const steps = Array.from(document.querySelectorAll('.step-item'));
    const inc   = 100 / steps.length;
    steps.forEach((step, i) => {
      if (pct >= inc * (i+1) && !step.classList.contains('completed')) {
        step.classList.add('completed');
        const span = document.createElement('span');
        span.textContent = `Completado a las ${
          new Date().toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})
        }`;
        const btn = step.querySelector('.step-actions .btn-primary');
        btn && btn.replaceWith(span);
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

  // resetea barra y tiempo restante completos
  document.querySelector('.progress-bar').style.width   = '0%';
  document.querySelector('.progress-value').textContent = '0%';
  const mins = Math.floor(activeDurationMs/60000);
  const secs = String(Math.floor((activeDurationMs%60000)/1000)).padStart(2,'0');
  document.getElementById('time-remaining').textContent = `${mins}m ${secs}s`;

  // hora de inicio vacía, se fijará al primer paso
  document.getElementById('start-time').textContent = '--';

  document.getElementById('total-duration').textContent = `${r.duracionMinutos} minutos`;
  document.getElementById('confirmFinishBtn').disabled  = false;

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
      // 1) Primer paso: arranca timer y fija hora de inicio
      if (idx === 0) {
        const t0 = Date.now();
        storeStart(activeReservationId, t0);
        document.getElementById('start-time').textContent =
          new Date(t0).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'});
        startAutoProgress(t0);

        step.classList.add('completed');
        const span = document.createElement('span');
        span.textContent = `Completado a las ${document.getElementById('start-time').textContent}`;
        btn.replaceWith(span);
        return;
      }

      // 2) Resto de pasos: incrementar barra
      step.classList.add('completed');
      const span = document.createElement('span');
      span.textContent = `Completado a las ${
        new Date().toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})
      }`;
      btn.replaceWith(span);

      let curr = parseInt(document.querySelector('.progress-value').textContent);
      const next = Math.min(100, curr + increment);
      document.querySelector('.progress-bar').style.width   = `${next}%`;
      document.querySelector('.progress-value').textContent = `${next}%`;

      // recalcula startTime para no retroceder
      const elapsed   = (next/100) * activeDurationMs;
      const newStart  = Date.now() - elapsed;
      storeStart(activeReservationId, newStart);
      clearInterval(autoProgressInterval);
      startAutoProgress(newStart);

      // persiste en backend
      await fetch(`${API_BASE}/progreso/${activeReservationId}?progreso=${next}`, {
        method:'PUT', credentials:'include'
      });

      // habilita Finalizar si todos completados
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
  if (!res.ok) return console.error('❌ completar', res.status);

  // fuerza UI al 100%
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
