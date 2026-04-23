package br.com.ctkd.dto.response;

import lombok.Builder;

@Builder
public record AuthResponse(String token) {
}
