package cz.animalhouse.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cz.animalhouse.dto.GeneCreateRequest;
import cz.animalhouse.dto.GeneResponse;
import cz.animalhouse.service.GeneService;

@WebMvcTest(GeneController.class)
class GeneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeneService geneService;

    @Test
    void shouldReturnAllGenes() throws Exception {

        when(geneService.findAll())
                .thenReturn(List.of(
                        new GeneResponse(1L, "OT1", "Ovalbumin-specific T-cell receptor"),
                        new GeneResponse(2L, "GFP", "Green fluorescent protein")));

        mockMvc.perform(get("/api/genes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].symbol").value("OT1"))
                .andExpect(jsonPath("$[0].description").value("Ovalbumin-specific T-cell receptor"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].symbol").value("GFP"))
                .andExpect(jsonPath("$[1].description").value("Green fluorescent protein"));
    }

    @Test
    void shouldCreateGene() throws Exception {

        when(geneService.create(any(GeneCreateRequest.class)))
                .thenReturn(new GeneResponse(
                        1L,
                        "GFP",
                        "Green fluorescent protein"));

        mockMvc.perform(post("/api/genes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "symbol": "GFP",
                                  "description": "Green fluorescent protein"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.symbol").value("GFP"))
                .andExpect(jsonPath("$.description").value("Green fluorescent protein"));
    }

    @Test
    void shouldRejectCreateGeneWithoutSymbol() throws Exception {

        mockMvc.perform(post("/api/genes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Missing symbol"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldReturnNotFoundWhenDeletingMissingGene()
            throws Exception {

        when(geneService.delete(999L))
                .thenReturn(false);

        mockMvc.perform(
                        delete("/api/transgenic-lines/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteExistingGene() throws Exception {
    	when(geneService.delete(7L))
        .thenReturn(true);

        mockMvc.perform(delete("/api/genes/{id}", 7L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(geneService).delete(7L);
        verifyNoMoreInteractions(geneService);
    }
}
