package com.subsonicfestival.frontend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Comprueba que el contexto completo arranca y que las rutas de acceso no quedaron
 * duplicadas tras la fusion de develop.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FrontendApplicationSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginAliasAndAccountRouterCoexist() throws Exception {
        mockMvc.perform(get("/login.html"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));

        mockMvc.perform(get("/cuenta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void adminArtistsRequiresAnAuthenticatedSession() throws Exception {
        mockMvc.perform(get("/admin/artistas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?motivo=caducada"));
    }
}
