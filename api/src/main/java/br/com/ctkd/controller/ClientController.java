package br.com.ctkd.controller;

import br.com.ctkd.dto.response.ClientResponse;
import br.com.ctkd.dto.request.ClientRequest;
import br.com.ctkd.factory.ClientFactory;
import br.com.ctkd.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Clients", description = "Client management")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {

    private final ClientService service;
    private final ClientFactory factory;

    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get client by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<ClientResponse> getClientById(
            @Parameter(description = "Client UUID") @PathVariable String id) {
        var client = service.getById(id);
        var response = factory.toClientResponse(client);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "List all clients")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client list returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<ClientResponse>> listClients() {
        var clients = service.getListClients();
        var response = factory.toClientResponse(clients);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/pageable")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "List clients with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated client list returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<ClientResponse>> pageClients(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "15") @RequestParam(defaultValue = "15") int size) {

        var response = service.pageClients(page, size)
                .map(factory::toClientResponse);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a client")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required")
    })
    public ResponseEntity<ClientResponse> saveClient(@RequestBody @Valid ClientRequest request) {
        var clientToSave = factory.toClient(request);
        var clientSaved = service.insertClient(clientToSave);
        var response = factory.toClientResponse(clientSaved);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a client")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Client updated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<ClientResponse> updateClient(
            @Parameter(description = "Client UUID") @PathVariable String id,
            @RequestBody @Valid ClientRequest request) {

        var clientToUpdate = factory.toClient(request);
        var clientUpdated = service.updateClient(clientToUpdate, id);
        var response = factory.toClientResponse(clientUpdated);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a client")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Client deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<Void> deleteClient(
            @Parameter(description = "Client UUID") @PathVariable String id) {
        service.softDelete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
