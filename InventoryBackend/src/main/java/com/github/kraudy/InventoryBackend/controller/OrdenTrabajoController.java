package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.model.OrdenTrabajo;
import com.github.kraudy.InventoryBackend.dto.UsuarioNombreDTO;
import com.github.kraudy.InventoryBackend.repository.OrdenTrabajoRepository;
import com.github.kraudy.InventoryBackend.service.OrdenTrabajoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ordenes-trabajo")
@CrossOrigin(origins = "http://localhost:4200")  // Prevents Cors error. Allow Angular dev server
public class OrdenTrabajoController {
  
  @Autowired
  private OrdenTrabajoRepository ordenTrabajoRepository;

  @Autowired
  private OrdenTrabajoService ordenTrabajoService;


  /* Registra progreso de trabajo, aplica para reparadores, normal y pegado */
  @PostMapping("/progreso-trabajo/{idOrden}/{idOrdenDetalle}/{cantidadTrabajada}")
  public OrdenTrabajo progresoTrabajo(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @PathVariable int cantidadTrabajada) {

    return ordenTrabajoService.progresoTrabajo(idOrden, idOrdenDetalle, cantidadTrabajada);
  }

  @GetMapping("/obtener-reparador/{idOrden}/{idOrdenDetalle}")
  public UsuarioNombreDTO getReparadores(
            @PathVariable Long idOrden, 
            @PathVariable Long idOrdenDetalle) {

    return ordenTrabajoRepository.getReparador(idOrden, idOrdenDetalle);
  }

  @GetMapping("/obtener-normal/{idOrden}/{idOrdenDetalle}")
  public UsuarioNombreDTO getNormal(
            @PathVariable Long idOrden, 
            @PathVariable Long idOrdenDetalle) {

    return ordenTrabajoRepository.getNormal(idOrden, idOrdenDetalle);
  }

  /**
   * Asigna el trabajo inicial a un estado
   * Este permite saber cuanto trabajo tiene un usuario en un estado determinado.
   * Posteriormente, el progreso de dicho trabajo se acualiza con /progreso-trabajo
   * Una vez se ha completado total o parcialmente, debe avanzarse al siguiente estado con OrdenSeguimientoController.advanceState
  */
  @PostMapping("/asignar-trabajo/{idOrden}/{idOrdenDetalle}/{trabajador}")
  public OrdenTrabajo asignarTrabajo(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @PathVariable String trabajador) {

      return ordenTrabajoService.asignarTrabajo(idOrden, idOrdenDetalle, trabajador);
  }
  
}