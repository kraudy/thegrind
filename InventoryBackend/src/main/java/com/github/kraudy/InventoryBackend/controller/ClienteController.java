package com.github.kraudy.InventoryBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.github.kraudy.InventoryBackend.model.Cliente;
import com.github.kraudy.InventoryBackend.repository.ClienteRepository;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "http://localhost:4200")
public class ClienteController {
  @Autowired
  private ClienteRepository clienteRepository;

  @GetMapping
  public List<Cliente> getAll() {
    return clienteRepository.findAll();
  }

  @GetMapping("/{id}")
  public Cliente getById(@PathVariable Long id) {
    return clienteRepository.findById(id).orElse(null);
  }

  @PostMapping
  public Cliente create(@RequestBody Cliente cliente) {
    return clienteRepository.save(cliente);
  }

  @PutMapping("/{id}")
  public Cliente update(@PathVariable Long id, @RequestBody Cliente cliente) {
    cliente.setId(id);
    return clienteRepository.save(cliente);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    clienteRepository.deleteById(id);
  }

  @GetMapping("/buscar")
  public List<Cliente> search(@RequestParam("termino") String termino) {
    if (termino == null || termino.trim().isEmpty()) return List.of();
    String term = termino.trim();
    return clienteRepository.searchByTerm(term);
  }
}
