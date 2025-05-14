package com.example.SlushyApp.Controller;
import com.example.SlushyApp.Service.VehiculoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class WebControllerAdmin {

    private final VehiculoService vehiculoService;

    public WebControllerAdmin(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("vehiculos", vehiculoService.obtenerTodos());
        return "admin/adminDashboard";
    }

    @GetMapping("/dashboard/vehiculos")
    public String adminDashboardVehiculos() {
        return "admin/adminDashboardVehiculos";
    }

    @GetMapping("/dashboard/clientes")
    public String adminDashboardClientes() {
        return "admin/adminDashboardClientes";
    }

    @GetMapping("/dashboard/empleados")
    public String adminDashboardEmpleados() {
        return "admin/adminDashboardEmpleados";
    }


}
