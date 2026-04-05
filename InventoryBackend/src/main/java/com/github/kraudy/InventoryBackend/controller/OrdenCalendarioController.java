package com.github.kraudy.InventoryBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.github.kraudy.InventoryBackend.dto.CalendarioDiaDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenCalendarioDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenDetalleDTO;
import com.github.kraudy.InventoryBackend.model.Orden;
import com.github.kraudy.InventoryBackend.model.Producto;
import com.github.kraudy.InventoryBackend.model.ProductoTipoEstado;
import com.github.kraudy.InventoryBackend.model.ProductoTipoEstadoPK;
import com.github.kraudy.InventoryBackend.model.OrdenCalendario;
import com.github.kraudy.InventoryBackend.model.OrdenDetalle;
import com.github.kraudy.InventoryBackend.model.OrdenDetallePK;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimiento;
import com.github.kraudy.InventoryBackend.repository.OrdenCalendarioRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenDetalleRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenSeguimientoRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenTrabajoRepository;
import com.github.kraudy.InventoryBackend.repository.ProductoRepository;
import com.github.kraudy.InventoryBackend.repository.ProductoTipoEstadoRepository;
import com.github.kraudy.InventoryBackend.service.OrdenCalendarioService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ordenes-calendario")
@CrossOrigin(origins = "http://localhost:4200") 
public class OrdenCalendarioController {
  @Autowired
  private OrdenCalendarioRepository ordenCalendarioRepository;

  @Autowired
  private OrdenRepository ordenRepository;

  @Autowired
  private OrdenSeguimientoRepository ordenSeguimientoRepository;

  @Autowired
  private OrdenDetalleRepository ordenDetalleRepository;

  @Autowired
  private ProductoRepository productoRepository;

  @Autowired
  private ProductoTipoEstadoRepository productoTipoEstadoRepository;

  @Autowired
  private OrdenTrabajoRepository ordenTrabajoRepository;

  @Autowired
  private OrdenCalendarioService ordenCalendarioService;
  
  
  @GetMapping
  public List<OrdenCalendarioDTO> getAll() {
    return ordenCalendarioRepository.getAllOrdenCalendario();
  }

  @GetMapping("/{id}")
  public OrdenCalendarioDTO getById(@PathVariable Long id) {
    return ordenCalendarioRepository.getByIdOrdenCalendario(id);
  }

  @GetMapping("/total-por-fecha/{fecha}")
  public Long getTotalPorFecha(@PathVariable String fecha) {
      return ordenCalendarioRepository.getTotalPorFecha(fecha);
  }

  @GetMapping("/calendario")
  public List<CalendarioDiaDTO> getCalendarioSemanal() {
    List<Object[]> rawDays = ordenCalendarioRepository.getCalendarDaysWithCountsRaw();
    List<OrdenCalendarioDTO> allOrders = ordenCalendarioRepository.getOrdersInTwoWeeks();

    // Group orders by date
    Map<LocalDate, List<OrdenCalendarioDTO>> ordersByDate = allOrders.stream()
            .collect(Collectors.groupingBy(OrdenCalendarioDTO::fecha));

    return rawDays.stream().map(row -> {
        LocalDate date = (LocalDate) row[0];
        return new CalendarioDiaDTO(
            date,
            (String) row[1],
            (String) row[2],
            (String) row[3],
            ((Number) row[4]).intValue(),
            ordersByDate.getOrDefault(date, List.of())
        );
    }).toList();
  }

  @PostMapping
  public OrdenCalendario create(@RequestBody OrdenCalendario ordenCalendario) {
    return ordenCalendarioService.create(ordenCalendario);
  }


  @PutMapping("/{id}")
  public OrdenCalendario update(@PathVariable Long id, @RequestBody OrdenCalendario ordenCalendario) {
    ordenCalendario.setIdOrden(id);
    return ordenCalendarioRepository.save(ordenCalendario);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    // Verificar que todos los detalles estén en su primer estado antes de eliminar
    if (!ordenSeguimientoRepository.areAllDetailsInFirstState(id)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
        "No se puede eliminar la orden del calendario porque algunos detalles ya han avanzado en su proceso de producción");
    }

    //TODO: Deberia eleminar el historico tambien?
    
    /* Elimina el trabajo de la orden */
    ordenTrabajoRepository.deleteByOrden(id);

    //TODO: Try to move to cascade
    /* Elimina el seguimiento de los detalles */
    ordenSeguimientoRepository.deleteByOrden(id);

    /* Elimina el calendario de la orden */
    ordenCalendarioRepository.deleteById(id);

    ordenRepository.updateEstado(id, "Recibida"); // Deja la orden como recibida para que pueda ser re-agendada
  }

}
