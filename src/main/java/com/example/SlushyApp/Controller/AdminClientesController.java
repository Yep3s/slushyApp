package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.Membresia;
import com.example.SlushyApp.Model.Reserva;
import com.example.SlushyApp.Model.Usuario;
import com.example.SlushyApp.Model.Vehiculo;
import com.example.SlushyApp.Model.Rol;
import com.example.SlushyApp.Repository.ReservaRepository;
import com.example.SlushyApp.Repository.UsuarioRepository;
import com.example.SlushyApp.Repository.VehiculoRepository;
import com.example.SlushyApp.Utils.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/clientes")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminClientesController {

    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ReservaRepository reservaRepository;
    private final JwtUtil jwtUtil;

    public AdminClientesController(UsuarioRepository usuarioRepository,
                                   VehiculoRepository vehiculoRepository,
                                   ReservaRepository reservaRepository,
                                   JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.reservaRepository = reservaRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Estadísticas solo para usuarios con rol USER
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticasClientes() {
        long total = usuarioRepository.countByRolesContains(Rol.USER);
        long vip = usuarioRepository.countByRolesContainsAndMembresia(Rol.USER, Membresia.VIP);
        long standard = usuarioRepository.countByRolesContainsAndMembresia(Rol.USER, Membresia.STANDARD);
        long nuevos = usuarioRepository.countByRolesContainsAndFechaRegistroAfter(
                Rol.USER,
                LocalDate.now().minusDays(7)
        );

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("vip", vip);
        stats.put("standard", standard);
        stats.put("nuevos", nuevos);

        return ResponseEntity.ok(stats);
    }

    /**
     * Paginado para usuarios con rol USER, opcional filtro por membresía
     */
    @GetMapping("/pagina")
    public ResponseEntity<Map<String, Object>> obtenerClientesPaginados(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "6") int tamaño,
            @RequestParam(required = false) String membresia) {

        Pageable pageable = PageRequest.of(pagina - 1, tamaño);
        Page<Usuario> page;

        if (membresia != null && !membresia.isEmpty()) {
            page = usuarioRepository.findByRolesContainsAndMembresia(
                    Rol.USER,
                    Membresia.valueOf(membresia.toUpperCase()),
                    pageable
            );
        } else {
            page = usuarioRepository.findByRolesContains(Rol.USER, pageable);
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("clientes", page.getContent());
        respuesta.put("totalPaginas", page.getTotalPages());

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Listado completo de clientes (rol USER)
     */
    @GetMapping("/todos")
    public ResponseEntity<List<Usuario>> obtenerTodosLosClientes() {
        List<Usuario> clientes = usuarioRepository
                .findByRolesContains(Rol.USER, Pageable.unpaged())
                .getContent();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/detalle")
    public ResponseEntity<Map<String,Object>> obtenerDetalleCliente(@RequestParam("email") String email) {
        // 1) Buscamos al usuario
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null || !usuario.getRoles().contains(Rol.USER)) {
            return ResponseEntity.notFound().build();
        }

        // 2) Todas las órdenes de los repositorios
        List<Vehiculo> vehiculos = vehiculoRepository.findByUsuarioEmail(email);

        // Asegúrate de que este método exista en ReservaRepository:
        // List<Reserva> findByUsuarioEmailOrderByFechaReservaDesc(String email);
        List<Reserva>  reservas  = reservaRepository.findByUsuarioEmailOrderByFechaInicioDesc(email);

        // 3) Empaquetamos la respuesta
        Map<String,Object> respuesta = new HashMap<>();
        respuesta.put("usuario",   usuario);
        respuesta.put("vehiculos", vehiculos);
        respuesta.put("reservas",  reservas);

        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/editar")
    public ResponseEntity<?> editarCliente(@RequestBody Usuario clienteEditado) {
        Usuario existente = usuarioRepository.findByEmail(clienteEditado.getEmail());
        if (existente == null || !existente.getRoles().contains(Rol.USER)) {
            return ResponseEntity.notFound().build();
        }

        existente.setNombre(clienteEditado.getNombre());
        existente.setApellido(clienteEditado.getApellido());
        existente.setCedula(clienteEditado.getCedula());
        existente.setTelefono(clienteEditado.getTelefono());
        existente.setMembresia(clienteEditado.getMembresia());

        usuarioRepository.save(existente);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/eliminar")
    public ResponseEntity<?> eliminarCliente(@RequestParam String email) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null || !usuario.getRoles().contains(Rol.USER)) {
            return ResponseEntity.notFound().build();
        }

        usuarioRepository.delete(usuario);
        return ResponseEntity.ok("Cliente eliminado");
    }
}
