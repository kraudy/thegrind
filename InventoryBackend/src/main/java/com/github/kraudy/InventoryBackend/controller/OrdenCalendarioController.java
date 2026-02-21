package com.github.kraudy.InventoryBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.github.kraudy.InventoryBackend.dto.CalendarioDiaDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenCalendarioDTO;
import com.github.kraudy.InventoryBackend.model.Orden;
import com.github.kraudy.InventoryBackend.model.OrdenCalendario;
import com.github.kraudy.InventoryBackend.repository.OrdenCalendarioRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenRepository;

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
  
  
  @GetMapping
  public List<OrdenCalendarioDTO> getAll() {
    return ordenCalendarioRepository.getAllOrdenCalendario();
  }

  @GetMapping("/{id}")
  public OrdenCalendarioDTO getById(@PathVariable Long id) {
    return ordenCalendarioRepository.getByIdOrdenCalendario(id);
  }

  //@GetMapping("/por-fecha/{fecha}")
  //public List<OrdenCalendarioDTO> getByFecha(@PathVariable String fecha) {
  //    return ordenCalendarioRepository.getByFecha(fecha);
  //}

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
    
    if (ordenCalendario.getIdOrden() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idOrden es obligatorio");
    }

    // Cargamos la entidad Orden real
    Orden orden = ordenRepository.findById(ordenCalendario.getIdOrden())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
            "Orden con id " + ordenCalendario.getIdOrden() + " no encontrada"));

    ordenCalendario.setOrden(orden);
    ordenCalendario.setFecha(ordenCalendario.getFechaTrabajo().toLocalDate());

    orden.setEstado("Repartida"); // Actualiza el estado de la orden
    ordenRepository.save(orden);

    return ordenCalendarioRepository.save(ordenCalendario);
  }


  @PutMapping("/{id}")
  public OrdenCalendario update(@PathVariable Long id, @RequestBody OrdenCalendario ordenCalendario) {
    ordenCalendario.setIdOrden(id);
    return ordenCalendarioRepository.save(ordenCalendario);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    ordenCalendarioRepository.deleteById(id);
  }

}
