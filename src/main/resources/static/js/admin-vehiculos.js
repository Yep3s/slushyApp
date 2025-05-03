let idVehiculoAEditar = null;
let idVehiculoAEliminar = null;
let tipoSeleccionado = "";
let paginaActual = 1;

document.addEventListener("DOMContentLoaded", () => {
    cargarEstadisticas();
    cargarTablaVehiculosPaginado();

    //Funcion para buscar en la searchBox

    document.querySelector(".search-box input").addEventListener("input", function () {
        const query = this.value.trim();

        if (query === "") {
            cargarTablaVehiculosPaginado(); // si está vacío, mostrar todo
            return;
        }

        fetch(`/admin/vehiculos/buscar-multiple?query=${encodeURIComponent(query)}`)
            .then(res => res.json())
            .then(data => {
                const tbody = document.querySelector(".table tbody");
                tbody.innerHTML = "";

                data.forEach(v => {
                    const row = document.createElement("tr");
                    row.innerHTML = `
                        <td>${v.id}</td>
                        <td>${v.placa}</td>
                        <td>${v.marca}</td>
                        <td>${v.linea}</td>
                        <td>${v.modelo}</td>
                        <td>${v.color}</td>
                        <td>${v.tipoVehiculo}</td>
                        <td>${v.usuarioEmail}</td>
                        <td>
                            <div class="table-actions">
                                <a href="#" class="table-action edit-btn" data-id="${v.id}"><i class="fas fa-edit"></i></a>
                                <a href="#" class="table-action delete-btn" data-id="${v.id}"><i class="fas fa-trash"></i></a>
                            </div>
                        </td>
                    `;
                    tbody.appendChild(row);
                });

                // 🛠️ Reasignar eventos a los nuevos botones
                asignarEventosTabla();
            });
    });


   //Cerrar Modal Automatico
   function closeModalFunc() {
       document.getElementById("addVehicleModal").classList.remove("active");
   }

    //Abrir Modal Agregar Vehiculo
    document.getElementById("btnAddVehicle").addEventListener("click", () => {
        document.getElementById("addVehicleModal").classList.add("active");
    });

    //Cerrar modales
    document.querySelectorAll(".modal-close-btn, .modal-close").forEach(btn => {
        btn.addEventListener("click", () => {
            document.querySelectorAll(".modal").forEach(modal => modal.classList.remove("active"));
        });
    });

    //Cerrar Modales Con ESC
    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            document.querySelectorAll(".modal.active").forEach(modal => modal.classList.remove("active"));
        }
    });

    // Filtro por tipo
    document.querySelectorAll('.chart-filter').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.chart-filter').forEach(f => f.classList.remove('active'));
            btn.classList.add('active');
            tipoSeleccionado = btn.textContent.trim() === "Todos" ? "" : btn.textContent.trim().toUpperCase();
            cargarTablaVehiculosPaginado(1, tipoSeleccionado);
        });
    });

    // Guardar nuevo vehículo
    document.getElementById("vehicleForm").addEventListener("submit", async function (e) {
        e.preventDefault();

        const nuevoVehiculo = {
            placa: document.getElementById("vehicle-plate").value,
            marca: document.getElementById("vehicle-make").value,
            linea: document.getElementById("vehicle-line").value,
            modelo: document.getElementById("vehicle-model").value,
            color: document.getElementById("vehicle-color").value,
            tipoVehiculo: document.getElementById("vehicle-type").value,
            usuarioEmail: document.getElementById("vehicle-email").value
        };

        const response = await fetch("/admin/vehiculos/agregar", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(nuevoVehiculo)
        });

        if (response.ok) {
            mostrarAlerta("Vehículo agregado correctamente");
            closeModalFunc();
            this.reset();
            cargarEstadisticas();
            cargarTablaVehiculosPaginado(paginaActual, tipoSeleccionado);
        }
    });

    // Guardar edición
    document.getElementById("saveVehicleChanges").addEventListener("click", async () => {
        const actualizado = {
            placa: document.getElementById("edit-vehicle-plate").value,
            marca: document.getElementById("edit-vehicle-make").value,
            linea: document.getElementById("edit-vehicle-line").value,
            modelo: document.getElementById("edit-vehicle-model").value,
            color: document.getElementById("edit-vehicle-color").value,
            tipoVehiculo: document.getElementById("edit-vehicle-type").value,
            usuarioEmail: document.getElementById("edit-vehicle-email").value
        };

        const response = await fetch(`/admin/vehiculos/editar/${idVehiculoAEditar}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(actualizado)
        });

        if (response.ok) {
            mostrarAlerta("Vehículo actualizado correctamente");
            document.getElementById("editVehicleModal").classList.remove("active");
            cargarEstadisticas();
            cargarTablaVehiculosPaginado(paginaActual, tipoSeleccionado);
        }
    });

    // Confirmar eliminación
    document.getElementById("eliminarVehiculo").addEventListener("click", async () => {
        const response = await fetch(`/admin/vehiculos/eliminar/${idVehiculoAEliminar}`, {
            method: "DELETE"
        });

        if (response.ok) {
            mostrarAlerta("Vehículo eliminado correctamente", true);
            document.getElementById("deleteVehicleModal").classList.remove("active");
            cargarEstadisticas();
            cargarTablaVehiculosPaginado(paginaActual, tipoSeleccionado);
        }
    });
});

function mostrarAlerta(texto, esError = false) {
    const alerta = esError ? document.getElementById("deleteVehicleAlert") : document.getElementById("saveVehicleAlert");
    alerta.querySelector(".alert-text").textContent = texto;
    alerta.classList.add("show");
    setTimeout(() => alerta.classList.remove("show"), 3000);
}

function cargarEstadisticas() {
    fetch("/admin/vehiculos/total")
        .then(res => res.json())
        .then(total => document.getElementById("total-vehiculos").textContent = total);

    fetch("/admin/vehiculos/contar-por-tipo")
        .then(res => res.json())
        .then(data => {
            document.getElementById("total-automovil").textContent = data.AUTOMOVIL || 0;
            document.getElementById("total-camioneta").textContent = data.CAMIONETA || 0;
            document.getElementById("total-moto").textContent = data.MOTO || 0;
        });
}

function cargarTablaVehiculosPaginado(pagina = 1, tipo = "") {
    paginaActual = pagina;

    fetch(`/admin/vehiculos/pagina?pagina=${pagina}&tipo=${tipo}`)
        .then(res => res.json())
        .then(data => {
            const tbody = document.querySelector(".table tbody");
            tbody.innerHTML = "";

            data.vehiculos.forEach(v => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${v.id}</td>
                    <td>${v.placa}</td>
                    <td>${v.marca}</td>
                    <td>${v.linea}</td>
                    <td>${v.modelo}</td>
                    <td>${v.color}</td>
                    <td>${v.tipoVehiculo}</td>
                    <td>${v.usuarioEmail}</td>
                    <td>
                        <div class="table-actions">
                            <a href="#" class="table-action edit-btn" data-id="${v.id}" data-vehiculo='${JSON.stringify(v)}'><i class="fas fa-edit"></i></a>
                            <a href="#" class="table-action delete-btn" data-id="${v.id}" data-marca="${v.marca}" data-linea="${v.linea}" data-placa="${v.placa}"><i class="fas fa-trash"></i></a>
                        </div>
                    </td>
                `;
                tbody.appendChild(row);
            });

            agregarEventosBotones();

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
        btn.addEventListener("click", () => cargarTablaVehiculosPaginado(i, tipoSeleccionado));
        paginacion.appendChild(btn);
    }
}

function agregarEventosBotones() {
    document.querySelectorAll(".edit-btn").forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            const v = JSON.parse(btn.getAttribute("data-vehiculo"));
            idVehiculoAEditar = v.id;

            document.getElementById("edit-vehicle-plate").value = v.placa;
            document.getElementById("edit-vehicle-make").value = v.marca;
            document.getElementById("edit-vehicle-line").value = v.linea;
            document.getElementById("edit-vehicle-model").value = v.modelo;
            document.getElementById("edit-vehicle-color").value = v.color;
            document.getElementById("edit-vehicle-type").value = v.tipoVehiculo;
            document.getElementById("edit-vehicle-email").value = v.usuarioEmail;

            document.getElementById("editVehicleModal").classList.add("active");
        });
    });

    document.querySelectorAll(".delete-btn").forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            idVehiculoAEliminar = btn.getAttribute("data-id");

            const marca = btn.getAttribute("data-marca");
            const linea = btn.getAttribute("data-linea");
            const placa = btn.getAttribute("data-placa");

            document.querySelector(".vehicle-to-delete").innerHTML = `<strong>Vehículo:</strong> ${marca} ${linea} (${placa})`;
            document.getElementById("deleteVehicleModal").classList.add("active");
        });
    });
}

function asignarEventosTabla() {
    document.querySelectorAll(".edit-btn").forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            const row = btn.closest("tr");
            idVehiculoAEditar = btn.getAttribute("data-id");

            document.getElementById("edit-vehicle-plate").value = row.children[1].textContent;
            document.getElementById("edit-vehicle-make").value = row.children[2].textContent;
            document.getElementById("edit-vehicle-line").value = row.children[3].textContent;
            document.getElementById("edit-vehicle-model").value = row.children[4].textContent;
            document.getElementById("edit-vehicle-color").value = row.children[5].textContent;
            document.getElementById("edit-vehicle-type").value = row.children[6].textContent;
            document.getElementById("edit-vehicle-email").value = row.children[7].textContent;

            document.getElementById("editVehicleModal").classList.add("active");
        });
    });

    document.querySelectorAll(".delete-btn").forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            idVehiculoAEliminar = btn.getAttribute("data-id");

            const row = btn.closest("tr");
            const marca = row.children[2].textContent;
            const linea = row.children[3].textContent;
            const placa = row.children[1].textContent;

            document.querySelector(".vehicle-to-delete").innerHTML = `<strong>Vehículo:</strong> ${marca} ${linea} (${placa})`;
            document.getElementById("deleteVehicleModal").classList.add("active");
        });
    });
}



