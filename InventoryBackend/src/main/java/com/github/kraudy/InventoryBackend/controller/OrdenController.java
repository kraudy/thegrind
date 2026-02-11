package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.model.Cliente;
import com.github.kraudy.InventoryBackend.model.Orden;

import com.github.kraudy.InventoryBackend.repository.ClienteRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin(origins = "http://localhost:4200")  // Prevents Cors error. Allow Angular dev server
public class OrdenController {

  @Autowired
  private OrdenRepository ordenRepository;

  @Autowired
  private ClienteRepository clienteRepository;

  @GetMapping
  public List<Orden> getAll() {
    return ordenRepository.findAll();
  }

  @GetMapping("/{id}")
  public Orden getById(@PathVariable Long id) {
    return ordenRepository.findById(id).orElse(null);
  }

  @PostMapping
  public Orden create(@RequestBody Orden orden) {
    setClienteFromIdCliente(orden); // ← resolve cliente
    return ordenRepository.save(orden);
  }

  @PutMapping("/{id}")
  public Orden update(@PathVariable Long id, @RequestBody Orden orden) {
    orden.setId(id);
    setClienteFromIdCliente(orden); // ← resolve cliente (in case it changed)
    return ordenRepository.save(orden);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    ordenRepository.deleteById(id);
  }

  private void setClienteFromIdCliente(Orden orden) {
    if (orden.getIdCliente() == null || orden.getIdCliente() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idCliente is required and must be > 0");
    }
    Cliente cliente = clienteRepository.findById(orden.getIdCliente())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente with id " + orden.getIdCliente() + " not found"));
    orden.setCliente(cliente);
  }

}
