package com.example.SlushyApp.Service;

import com.example.SlushyApp.DTO.DashboardStatsDto;
import com.example.SlushyApp.Repository.PagoRepository;
import com.example.SlushyApp.Repository.ReservaRepository;
import com.example.SlushyApp.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardService {

    private final ReservaRepository reservaRepo;
    private final PagoRepository pagoRepo;
    private final UsuarioRepository usuarioRepo;

    public DashboardService(ReservaRepository reservaRepo,
                            PagoRepository pagoRepo,
                            UsuarioRepository usuarioRepo) {
        this.reservaRepo = reservaRepo;
        this.pagoRepo = pagoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    public DashboardStatsDto calcularStatsDeHoy() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin    = hoy.plusDays(1).atStartOfDay();

        // 1) Vehículos hoy = reservas iniciadas hoy
        long vehiculosHoy = reservaRepo.countByFechaInicioBetween(inicio, fin);

        // 2) Ingresos hoy = suma de montos de pagos hechos hoy
        Double ingresos = pagoRepo.sumMontoByFechaPagoBetween(inicio, fin);
        double ingresosHoy = ingresos != null ? ingresos : 0.0;

        // 3) Clientes nuevos hoy = usuarios registrados hoy
        long clientesNuevos = usuarioRepo.countByFechaRegistroBetween(inicio, fin);

        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setVehiculosHoy(vehiculosHoy);
        dto.setIngresosHoy(ingresosHoy);
        dto.setClientesNuevos(clientesNuevos);
        return dto;
    }


}
