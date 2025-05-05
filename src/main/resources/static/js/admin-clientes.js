// admin-clientes.js

document.addEventListener("DOMContentLoaded", () => {
    cargarEstadisticasClientes();
    cargarTablaClientes();

});

let membresiaSeleccionada = ""; // "", "VIP", "STANDARD"

    // Filtro por tipo
document.querySelectorAll(".chart-filter").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".chart-filter").forEach(f => f.classList.remove("active"));
        btn.classList.add("active");

        const tipo = btn.textContent.trim().toUpperCase();
        membresiaSeleccionada = (tipo === "TODOS") ? "" : tipo;

        cargarTablaClientes(1); // ¡Llama la función correcta de clientes!
    });
});

// admin-clientes.js (continuación o al final del archivo)

const inputBusqueda = document.querySelector(".search-box input");

inputBusqueda.addEventListener("input", () => {
    const termino = inputBusqueda.value.trim().toLowerCase();
    filtrarClientes(termino);
});

function filtrarClientes(termino) {
    const filas = document.querySelectorAll(".table tbody tr");

    filas.forEach(fila => {
        const email = fila.querySelector(".client-email").textContent.toLowerCase();
        const telefono = fila.children[2].textContent.toLowerCase();

        const coincide = email.includes(termino) || telefono.includes(termino);
        fila.style.display = coincide ? "" : "none";
    });
}

function cargarEstadisticasClientes() {
    fetch("/admin/clientes/estadisticas")
        .then(res => res.json())
        .then(data => {
            document.getElementById("total-clientes").textContent = data.total;
            document.getElementById("clientes-vip").textContent = data.vip;
            document.getElementById("clientes-standard").textContent = data.standard;
            document.getElementById("clientes-nuevos").textContent = data.nuevos;
        })
        .catch(err => console.error("Error al cargar estadísticas de clientes:", err));
}

        // Responsive sidebar toggle
        document.getElementById('menuToggle').addEventListener('click', function() {
            const sidebar = document.getElementById('adminSidebar');
            sidebar.classList.toggle('active');
        });

        // Modal functionality
        const modal = document.getElementById('addClientModal');
        const btnAddClient = document.getElementById('btnAddClient');
        const closeModal = document.getElementById('closeModal');
        const cancelBtn = document.getElementById('cancelBtn');

        btnAddClient.addEventListener('click', function() {
            modal.classList.add('show');
        });

        function closeModalFunc() {
            modal.classList.remove('show');
        }

        closeModal.addEventListener('click', closeModalFunc);
        cancelBtn.addEventListener('click', closeModalFunc);

        // Close modal when clicking outside
        window.addEventListener('click', function(event) {
            if (event.target === modal) {
                closeModalFunc();
            }
        });

        // Form submission (prevent default for demo)
        document.getElementById('clientForm').addEventListener('submit', function(e) {
            e.preventDefault();
            alert('Cliente guardado correctamente!');
            closeModalFunc();
        });
    const viewModal = document.getElementById('viewClientModal');
    const editModal = document.getElementById('editClientModal');
    const deleteModal = document.getElementById('deleteClientModal');

    // Botones de cierre
    const closeButtons = document.querySelectorAll('.modal-close');

    document.querySelectorAll(".chart-filter").forEach(btn => {
        btn.addEventListener("click", () => {
            document.querySelectorAll(".chart-filter").forEach(f => f.classList.remove("active"));
            btn.classList.add("active");

            const tipo = btn.textContent.trim().toUpperCase();
            membresiaSeleccionada = (tipo === "TODOS") ? "" : tipo;

            cargarTablaClientes(1);
        });
    });

    // Abrir modales desde la tabla
    document.querySelectorAll('.table-action[title="Ver detalles"]').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const row = this.closest('tr');
            fillViewModal(row);
            viewModal.classList.add('show');
        });
    });

    document.querySelectorAll('.table-action[title="Eliminar"]').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const row = this.closest('tr');
            fillDeleteModal(row);
            deleteModal.classList.add('show');
        });
    });

    // Cerrar modales
    closeButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            viewModal.classList.remove('show');
            editModal.classList.remove('show');
            deleteModal.classList.remove('show');
        });
    });

    // Cerrar al hacer clic fuera del modal
    window.addEventListener('click', function(e) {
        if (e.target === viewModal) viewModal.classList.remove('show');
        if (e.target === editModal) editModal.classList.remove('show');
        if (e.target === deleteModal) deleteModal.classList.remove('show');
    });

    // Formulario de edición
    document.getElementById('editClientForm').addEventListener('submit', function(e) {
        e.preventDefault();
        editModal.classList.remove('show');
        showAlert('saveAlert');
    });

    // Confirmar eliminación
    document.getElementById('confirmDelete').addEventListener('click', function() {
        deleteModal.classList.remove('show');
        showAlert('deleteAlert');
    });

    let paginaActual = 1;

function cargarTablaClientes(pagina = 1) {
    const query = `/admin/clientes/pagina?pagina=${pagina}&membresia=${membresiaSeleccionada}`;

    fetch(query)
        .then(res => res.json())
        .then(data => {
            const tbody = document.querySelector(".table tbody");
            tbody.innerHTML = "";

            data.clientes.forEach(c => {
                const avatar = `${c.nombre[0] || ''}${c.apellido[0] || ''}`.toUpperCase();
                const nombreCompleto = `${c.nombre} ${c.apellido}`;
                const email = c.email;
                const telefono = c.telefono || "";
                const membresia = c.membresia || "STANDARD";

                const fila = document.createElement("tr");
                fila.innerHTML = `
                    <td>${c.id}</td>
                    <td>
                        <div class="client-info">
                            <div class="client-avatar">${avatar}</div>
                            <div>
                                <div class="client-name">${nombreCompleto}</div>
                                <div class="client-email">${email}</div>
                            </div>
                        </div>
                    </td>
                    <td>${telefono}</td>
                    <td><span class="membership-badge ${membresia.toLowerCase()}">${membresia}</span></td>
                    <td>—</td>
                    <td>
                        <div class="table-actions">
                            <a href="#" class="table-action" title="Ver Cliente"><i class="fas fa-eye"></i></a>
                            <a href="#" class="table-action" title="Editar"><i class="fas fa-edit"></i></a>
                            <a href="#" class="table-action" title="Eliminar"><i class="fas fa-trash"></i></a>
                        </div>
                    </td>
                `;
                tbody.appendChild(fila);

                // Agregar listener al botón "Ver Cliente"
                const verBtn = fila.querySelector('.table-action[title="Ver Cliente"]');
                verBtn.addEventListener("click", (e) => {
                    e.preventDefault();
                    fetch(`/admin/clientes/detalle?email=${encodeURIComponent(email)}`)
                        .then(res => res.json())
                        .then(data => {
                            const u = data.usuario;
                            document.getElementById("client-detail-title").textContent = `Historial de Servicios - ${u.nombre} ${u.apellido}`;
                            document.getElementById("client-name").textContent = `${u.nombre} ${u.apellido}`;
                            document.getElementById("client-email").textContent = u.email;
                            document.getElementById("client-phone").textContent = u.telefono || "No registrado";
                            document.getElementById("client-membership").textContent = u.membresia;
                            document.getElementById("client-membership").className = `membership-badge ${u.membresia.toLowerCase()}`;
                            document.getElementById("client-since").textContent = new Date(u.fechaRegistro).toLocaleDateString("es-CO");

                            const vehiculosContainer = document.getElementById("client-vehicles");
                            vehiculosContainer.innerHTML = "";
                            data.vehiculos.forEach(v => {
                                const card = document.createElement("div");
                                card.classList.add("vehicle-card");
                                card.innerHTML = `
                                    <div class="vehicle-card-header">
                                        <h5>${v.marca} ${v.linea}</h5>
                                        <span class="vehicle-year">${v.modelo}</span>
                                    </div>
                                    <div class="vehicle-details">
                                        <span><i class="fas fa-palette"></i> ${v.color}</span>
                                        <span><i class="fas fa-tag"></i> ${v.placa}</span>
                                    </div>
                                `;
                                vehiculosContainer.appendChild(card);
                            });

                            const serviciosBody = document.getElementById("client-services");
                            serviciosBody.innerHTML = "";
                            data.reservas.forEach(r => {
                                const row = document.createElement("tr");
                                row.innerHTML = `
                                    <td>${new Date(r.fechaReserva).toLocaleDateString("es-CO")}</td>
                                    <td>${r.servicioNombre}</td>
                                    <td>${r.placaVehiculo}</td>
                                    <td>$${r.monto || "—"}</td>
                                    <td><span class="status-badge status-${r.estado.toLowerCase()}">${r.estado}</span></td>
                                `;
                                serviciosBody.appendChild(row);
                            });

                        })
                        .catch(err => console.error("Error al cargar detalle del cliente:", err));
                });

            });

            actualizarPaginacion(data.totalPaginas, pagina);
        });
}


    function actualizarPaginacion(totalPaginas, paginaActual) {
        const paginacion = document.querySelector(".pagination");
        paginacion.innerHTML = "";

        for (let i = 1; i <= totalPaginas; i++) {
            const btn = document.createElement("button");
            btn.classList.add("pagination-btn");
            if (i === paginaActual) btn.classList.add("active");
            btn.textContent = i;
            btn.addEventListener("click", () => {
                cargarTablaClientes(i);
            });
            paginacion.appendChild(btn);
        }
    }

    // Mostrar alertas
    function showAlert(alertId) {
        const alert = document.getElementById(alertId);
        alert.classList.add('show');

        setTimeout(() => {
            alert.classList.remove('show');
        }, 3000);
    }


