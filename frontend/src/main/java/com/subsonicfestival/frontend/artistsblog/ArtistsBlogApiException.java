package com.subsonicfestival.frontend.artistsblog;

public class ArtistsBlogApiException extends RuntimeException {

    public ArtistsBlogApiException(Throwable cause) {
        super("The Artists & Blog service is unavailable.", cause);
    }
}
