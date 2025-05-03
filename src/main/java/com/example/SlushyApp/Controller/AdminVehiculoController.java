package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.Vehiculo;
import com.example.SlushyApp.Service.VehiculoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/vehiculos")
public class AdminVehiculoController {

    private final VehiculoService vehiculoService;

    public AdminVehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    // ✅ Obtener todos los vehículos
    @GetMapping("/todos")
    public ResponseEntity<List<Vehiculo>> obtenerTodos() {
        return ResponseEntity.ok(vehiculoService.obtenerTodos());
    }

    // ✅ Contar total de vehículos
    @GetMapping("/total")
    public ResponseEntity<Long> contarTodos() {
        return ResponseEntity.ok(vehiculoService.contarTodos());
    }

    // ✅ Contar vehículos por tipo
    @GetMapping("/contar-por-tipo")
    public ResponseEntity<Map<String, Long>> contarPorTipo() {
        List<Vehiculo> todos = vehiculoService.obtenerTodos();
        Map<String, Long> conteo = todos.stream().collect(
                Collectors.groupingBy(
                        v -> v.getTipoVehiculo().name(), // Convertir enum a String
                        Collectors.counting()
                )
        );
        return ResponseEntity.ok(conteo);
    }

    // ✅ Buscar vehículos por placa (contiene)
    @GetMapping("/buscar")
    public ResponseEntity<List<Vehiculo>> buscarPorPlaca(@RequestParam String placa) {
        return ResponseEntity.ok(vehiculoService.buscarPorPlaca(placa));
    }

    // ✅ Filtrar por tipo
    @GetMapping("/filtrar")
    public ResponseEntity<List<Vehiculo>> filtrarPorTipo(@RequestParam String tipo) {
        return ResponseEntity.ok(vehiculoService.filtrarPorTipo(tipo));
    }

    // ✅ Agregar vehículo manualmente (con email especificado)
    @PostMapping("/agregar")
    public ResponseEntity<Vehiculo> agregarVehiculo(@RequestBody Vehiculo vehiculo) {
        return ResponseEntity.ok(vehiculoService.registrarVehiculo(vehiculo));
    }

    // ✅ Editar vehículo por ID (sin validar usuario)
    @PutMapping("/editar/{id}")
    public ResponseEntity<Vehiculo> editarVehiculo(@PathVariable String id, @RequestBody Vehiculo vehiculo) {
        return ResponseEntity.ok(vehiculoService.actualizarVehiculo(id, vehiculo));
    }

    // ✅ Eliminar vehículo por ID (sin validar usuario)
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarVehiculo(@PathVariable String id) {
        vehiculoService.eliminarVehiculoComoAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pagina")
    public ResponseEntity<Map<String, Object>> obtenerVehiculosPaginados(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String tipo
    ) {
        List<Vehiculo> lista = tipo != null && !tipo.isEmpty()
                ? vehiculoService.filtrarPorTipo(tipo)
                : vehiculoService.obtenerTodos();

        int total = lista.size();
        int totalPaginas = (int) Math.ceil((double) total / size);
        int inicio = (pagina - 1) * size;
        int fin = Math.min(inicio + size, total);

        List<Vehiculo> sublista = lista.subList(inicio, fin);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("vehiculos", sublista);
        respuesta.put("paginaActual", pagina);
        respuesta.put("totalPaginas", totalPaginas);

        return ResponseEntity.ok(respuesta);
    }


}
