package br.com.ctkd.controller;

import br.com.ctkd.dto.request.AuthRequest;
import br.com.ctkd.dto.response.AuthResponse;
import br.com.ctkd.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Obtain a JWT token")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with username and password and receive a JWT token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful — token returned"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
