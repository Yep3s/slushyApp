package com.example.SlushyApp.Controller;
import com.example.SlushyApp.Model.Vehiculo;
import com.example.SlushyApp.Service.VehiculoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class WebControllerUser {

    private final VehiculoService vehiculoService;

    public WebControllerUser(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping("/dashboard")
    public String userDashboard(Model model, Principal principal) {
        String emailUsuario = principal.getName();
        List<Vehiculo> vehiculos = vehiculoService.obtenerVehiculosPorUsuario(emailUsuario);
        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("cantidadVehiculos", vehiculos.size());
        return "user/userDashboard";
    }


}
