package com.github.kraudy.InventoryBackend.service;

import com.github.kraudy.InventoryBackend.model.Cliente;
import com.github.kraudy.InventoryBackend.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public Cliente create(Cliente cliente) {
        // Normalize names to Title Case
        cliente.setNombre(toTitleCase(cliente.getNombre()));
        cliente.setApellido(toTitleCase(cliente.getApellido()));

        // Prevent duplicates (case-insensitive)
        if (clienteRepository.existeCliente(cliente.getNombre(), cliente.getApellido())) {            
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un cliente con el mismo nombre y apellido");
        }

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente update(Cliente cliente) {
        // Normalize names to Title Case
        cliente.setNombre(toTitleCase(cliente.getNombre()));
        cliente.setApellido(toTitleCase(cliente.getApellido()));

        // Check duplicate but allow updating the same client
        if (clienteRepository.existeClienteDiferenteId(cliente.getNombre(), cliente.getApellido(), cliente.getId())) {            
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un cliente con el mismo nombre y apellido que desea actualizar");
        }

        return clienteRepository.save(cliente);
    }

    private String toTitleCase(String input) {
      if (input == null || input.trim().isEmpty()) {
          return input;
      }
      return java.util.Arrays.stream(input.trim().split("\\s+"))
              .map(word -> word.isEmpty() ? word :
                      word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
              .collect(java.util.stream.Collectors.joining(" "));
    }
}