package com.subsonicfestival.frontend.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Respuesta de {@code POST /v1/auth/login}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TokenResponse(String accessToken, String tokenType, int expiresIn) {
}
