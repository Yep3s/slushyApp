package com.example.SlushyApp.Controller;
import com.example.SlushyApp.Model.Membresia;
import com.example.SlushyApp.Model.Usuario;
import com.example.SlushyApp.Repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/clientes")
public class AdminClientesController {

    private final UsuarioRepository usuarioRepository;

    public AdminClientesController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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

}
