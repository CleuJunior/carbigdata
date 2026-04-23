package br.com.ctkd.service;

import br.com.ctkd.config.auth.JwtService;
import br.com.ctkd.dto.request.AuthRequest;
import br.com.ctkd.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        var token = jwtService.generateToken(request.username());
        return new AuthResponse(token);
    }
}
