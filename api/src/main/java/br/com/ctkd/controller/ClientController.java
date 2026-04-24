package br.com.ctkd.controller;

import br.com.ctkd.dto.response.ClientResponse;
import br.com.ctkd.dto.request.ClientRequest;
import br.com.ctkd.factory.ClientFactory;
import br.com.ctkd.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;
    private final ClientFactory factory;

    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable String id) {
        var client = service.getById(id);
        var response = factory.toClientResponse(client);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<ClientResponse>> listClients() {
        var clients = service.getListClients();
        var response = factory.toClientResponse(clients);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/pageable")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<ClientResponse>> pageClients(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "15") int size) {

        var response = service.pageClients(page, size)
                .map(factory::toClientResponse);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> saveClient(@RequestBody @Valid ClientRequest request) {
        var clientToSave = factory.toClient(request);
        var clientSaved = service.insertClient(clientToSave);
        var response = factory.toClientResponse(clientSaved);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> updateClient(@PathVariable String id,
                                                       @RequestBody @Valid ClientRequest request) {

        var clientToUpdate = factory.toClient(request);
        var clientUpdated = service.updateClient(clientToUpdate, id);
        var response = factory.toClientResponse(clientUpdated);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClient(@PathVariable String id) {
        service.softDelete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}