package com.subsonicfestival.frontend.artistsblog;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(annotations = Controller.class)
public class ArtistsBlogErrorHandler {

    @ExceptionHandler(ArtistsBlogApiException.class)
    public ModelAndView handleServiceUnavailable() {
        ModelAndView page = new ModelAndView("artists-blog/service-unavailable");
        page.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
        return page;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView handleRequestError(ResponseStatusException exception, Model model) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        model.addAttribute("status", status.value());
        model.addAttribute("message", exception.getReason());
        ModelAndView page = new ModelAndView("artists-blog/request-error", model.asMap());
        page.setStatus(status);
        return page;
    }
}
