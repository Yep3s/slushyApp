document.addEventListener('DOMContentLoaded', () => {
  const tabServices = document.querySelector('.user-tab[data-tab="services"]');
  const historyContainer = document.querySelector('#services-content .service-history');

  async function loadHistorial() {
    historyContainer.innerHTML = `
      <div class="service-history-header">
        <h3 class="service-history-title">Servicios Recientes</h3>
      </div>`;
    try {
      const res = await fetch('/user/reservas/historial', {
        credentials: 'include'
      });
      if (!res.ok) throw new Error(await res.text());
      const items = await res.json();

      items.forEach(r => {
        const d = new Date(r.fechaInicio);
        const fecha = d.toLocaleDateString('es-ES', {
          day: 'numeric', month: 'long', year: 'numeric'
        });
        const hora = d.toLocaleTimeString('es-ES', {
          hour: '2-digit', minute: '2-digit'
        });
        // Escoge un color según estado
        let colorBg = 'rgba(16,185,129,0.1)', colorTxt='var(--success)';
        if (r.estado === 'CANCELADA') {
          colorBg = 'rgba(239,68,68,0.1)'; colorTxt='var(--error)';
        } else if (r.estado === 'PENDIENTE') {
          colorBg = 'rgba(252,211,77,0.1)'; colorTxt='var(--warning)';
        }

        historyContainer.innerHTML += `
          <div class="service-item">
            <div class="service-icon"><i class="fas fa-soap"></i></div>
            <div class="service-details">
              <div class="service-title">
                <span>${r.servicioNombre}</span>
                <span class="service-status"
                      style="background:${colorBg};color:${colorTxt}">
                  ${r.estado}
                </span>
              </div>
              <div class="service-date">${fecha} – ${hora}</div>
              <div class="service-vehicle">${r.placaVehiculo}</div>
              <div class="service-price">$${r.monto.toLocaleString()}</div>
            </div>
          </div>`;
      });

      if (items.length === 0) {
        historyContainer.innerHTML += `<p>No tienes servicios en el historial.</p>`;
      }
    } catch (err) {
      console.error(err);
      historyContainer.innerHTML += `<p class="error">No se pudo cargar el historial.</p>`;
    }
  }

  // Cuando clickeas la pestaña, cargamos el historial
  tabServices.addEventListener('click', loadHistorial);

  // (Opcional) carga al abrir la página si esa pestaña está activa
  if (tabServices.classList.contains('active')) {
    loadHistorial();
  }
});
