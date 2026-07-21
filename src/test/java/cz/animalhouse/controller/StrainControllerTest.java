package cz.animalhouse.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cz.animalhouse.dto.StrainCreateRequest;
import cz.animalhouse.dto.StrainResponse;
import cz.animalhouse.service.StrainService;

@WebMvcTest(StrainController.class)
class StrainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StrainService strainService;

    @Test
    void shouldReturnAllStrains() throws Exception {
        StrainResponse strain1 = new StrainResponse(
                1L,
                "C57BL6",
                "C57BL/6"
        );

        StrainResponse strain2 = new StrainResponse(
                2L,
                "BALBC",
                "BALB/c"
        );

        when(strainService.findAll())
                .thenReturn(List.of(strain1, strain2));

        mockMvc.perform(get("/api/strain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("C57BL6"))
                .andExpect(jsonPath("$[0].name").value("C57BL/6"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].code").value("BALBC"))
                .andExpect(jsonPath("$[1].name").value("BALB/c"));

        verify(strainService).findAll();
    }

    @Test
    void shouldCreateStrain() throws Exception {
        StrainResponse response = new StrainResponse(
                1L,
                "C57BL6",
                "C57BL/6"
        );

        when(strainService.create(any(StrainCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/strain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "C57BL6",
                                  "name": "C57BL/6"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("C57BL6"))
                .andExpect(jsonPath("$.name").value("C57BL/6"));

        verify(strainService)
                .create(any(StrainCreateRequest.class));
    }

    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/api/strain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "",
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(strainService, never())
                .create(any(StrainCreateRequest.class));
    }

    @Test
    void shouldDeleteExistingStrain() throws Exception {
        Long id = 1L;

        when(strainService.delete(id))
                .thenReturn(true);

        mockMvc.perform(delete("/api/strain/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(strainService).delete(id);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingStrain()
            throws Exception {

        Long id = 999L;

        when(strainService.delete(id))
                .thenReturn(false);

        mockMvc.perform(delete("/api/strain/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));

        verify(strainService).delete(id);
    }

    @Test
    void shouldUpdateExistingStrain() throws Exception {
        Long id = 1L;

        StrainResponse response = new StrainResponse(
                id,
                "C57BL6J",
                "C57BL/6J"
        );

        when(strainService.update(
                eq(id),
                any(StrainCreateRequest.class)))
                .thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/strain/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "C57BL6J",
                                  "name": "C57BL/6J"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("C57BL6J"))
                .andExpect(jsonPath("$.name").value("C57BL/6J"));

        verify(strainService).update(
                org.mockito.ArgumentMatchers.eq(id),
                any(StrainCreateRequest.class)
        );
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingStrain()
            throws Exception {

        Long id = 999L;

        when(strainService.update(
                eq(id),
                any(StrainCreateRequest.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/strain/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "C57BL6J",
                                  "name": "C57BL/6J"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));

        verify(strainService).update(
                eq(id),
                any(StrainCreateRequest.class)
        );
    }

    @Test
    void shouldRejectInvalidUpdateRequest() throws Exception {
        Long id = 1L;

        mockMvc.perform(put("/api/strain/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "",
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(strainService, never()).update(
                eq(id),
                any(StrainCreateRequest.class)
        );
    }
}
