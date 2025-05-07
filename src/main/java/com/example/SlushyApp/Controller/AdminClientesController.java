package com.example.SlushyApp.Controller;
import com.example.SlushyApp.Model.Membresia;
import com.example.SlushyApp.Model.Reserva;
import com.example.SlushyApp.Model.Usuario;
import com.example.SlushyApp.Model.Vehiculo;
import com.example.SlushyApp.Repository.ReservaRepository;
import com.example.SlushyApp.Repository.UsuarioRepository;
import com.example.SlushyApp.Repository.VehiculoRepository;
import com.example.SlushyApp.Utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin/clientes")
public class AdminClientesController {

    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ReservaRepository reservaRepository;
    private final JwtUtil jwtUtil;


    public AdminClientesController(UsuarioRepository usuarioRepository,
                                           VehiculoRepository vehiculoRepository,
                                           ReservaRepository reservaRepository, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.reservaRepository = reservaRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticasClientes() {
        long total = usuarioRepository.count();
        long vip = usuarioRepository.countByMembresia(Membresia.VIP);
        long standard = usuarioRepository.countByMembresia(Membresia.STANDARD);
        long nuevos = usuarioRepository.countByFechaRegistroAfter(LocalDate.now().minusDays(7)); // últimos 7 días

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("vip", vip);
        stats.put("standard", standard);
        stats.put("nuevos", nuevos);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/pagina")
    public ResponseEntity<Map<String, Object>> obtenerClientesPaginados(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "6") int tamaño,
            @RequestParam(required = false) String membresia) {

        Pageable pageable = PageRequest.of(pagina - 1, tamaño);
        Page<Usuario> paginaUsuarios;

        if (membresia != null && !membresia.isEmpty()) {
            paginaUsuarios = usuarioRepository.findByMembresia(Membresia.valueOf(membresia.toUpperCase()), pageable);
        } else {
            paginaUsuarios = usuarioRepository.findAll(pageable);
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("clientes", paginaUsuarios.getContent()); // <--- debe ser "clientes" (NO "usuarios")
        respuesta.put("totalPaginas", paginaUsuarios.getTotalPages());


        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/todos")
    public List<Usuario> obtenerTodosLosClientes() {
        return usuarioRepository.findAll();
    }

    @GetMapping("/detalle")
    public ResponseEntity<?> obtenerDetalleCliente(@RequestParam String email) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) return ResponseEntity.notFound().build();

        List<Vehiculo> vehiculos = vehiculoRepository.findByUsuarioEmail(email);
        List<Reserva> reservas = reservaRepository.findByUsuarioEmailOrderByFechaReservaDesc(email);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("usuario", usuario);
        respuesta.put("vehiculos", vehiculos);
        respuesta.put("reservas", reservas);

        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/editar")
    public ResponseEntity<?> editarCliente(@RequestBody Usuario clienteEditado) {
        Usuario existente = usuarioRepository.findByEmail(clienteEditado.getEmail());
        if (existente == null) return ResponseEntity.notFound().build();

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
        if (usuario == null) return ResponseEntity.notFound().build();

        usuarioRepository.delete(usuario);
        return ResponseEntity.ok("Cliente eliminado");
    }


}
