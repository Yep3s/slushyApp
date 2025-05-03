// admin-clientes.js

document.addEventListener("DOMContentLoaded", () => {
    cargarEstadisticasClientes();
    cargarTablaClientes();

});

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
        fetch(`/admin/clientes/pagina?pagina=${pagina}`)
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
                                <a href="#" class="table-action" title="Editar"><i class="fas fa-edit"></i></a>
                                <a href="#" class="table-action" title="Eliminar"><i class="fas fa-trash"></i></a>
                            </div>
                        </td>
                    `;
                    tbody.appendChild(fila);
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


