package com.github.kraudy.InventoryBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.github.kraudy.InventoryBackend.dto.OrdenCalendarioDTO;
import com.github.kraudy.InventoryBackend.model.Orden;
import com.github.kraudy.InventoryBackend.model.OrdenCalendario;
import com.github.kraudy.InventoryBackend.repository.OrdenCalendarioRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenRepository;

import java.util.List;

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

  @GetMapping("/por-fecha/{fecha}")
  public List<OrdenCalendarioDTO> getByFecha(@PathVariable String fecha) {
      return ordenCalendarioRepository.getByFecha(fecha);
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
