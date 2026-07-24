package cz.animalhouse.controller;

import static org.hamcrest.Matchers.containsString;
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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cz.animalhouse.dto.GeneResponse;
import cz.animalhouse.dto.TransgenicLineGeneRequest;
import cz.animalhouse.dto.TransgenicLineGeneRowResponse;
import cz.animalhouse.entity.TransgenicLineGeneId;
import cz.animalhouse.exception.TransgenicLineGeneNotFoundException;
import cz.animalhouse.service.TransgenicLineGeneService;

@WebMvcTest(TransgenicLineGeneController.class)
class TransgenicLineGeneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransgenicLineGeneService service;

    @Test
    void shouldReturnAllAssignmentsGroupedByTransgenicLine()
            throws Exception {

        GeneResponse gene1 = new GeneResponse(
                100L,
                "OT1",
                "Ovalbumin-specific T-cell receptor"
        );

        GeneResponse gene2 = new GeneResponse(
                200L,
                "GFP",
                "Green fluorescent protein"
        );

        TransgenicLineGeneRowResponse row =
                new TransgenicLineGeneRowResponse(
                        10L,
                        "OT-I",
                        1L,
                        "C57BL6",
                        "C57BL/6",
                        List.of(gene1, gene2)
                );

        when(service.findAllGrouped())
                .thenReturn(List.of(row));

        mockMvc.perform(
                        get("/api/transgenic-line-genes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].transgenicLineId")
                        .value(10))
                .andExpect(jsonPath("$[0].transgenicLineName")
                        .value("OT-I"))
                .andExpect(jsonPath("$[0].strainId")
                        .value(1))
                .andExpect(jsonPath("$[0].strainCode")
                        .value("C57BL6"))
                .andExpect(jsonPath("$[0].strainName")
                        .value("C57BL/6"))
                .andExpect(jsonPath("$[0].genes.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].genes[0].id")
                        .value(100))
                .andExpect(jsonPath("$[0].genes[0].symbol")
                        .value("OT1"))
                .andExpect(jsonPath("$[0].genes[1].id")
                        .value(200))
                .andExpect(jsonPath("$[0].genes[1].symbol")
                        .value("GFP"));

        verify(service).findAllGrouped();
    }

    @Test
    void shouldReturnEmptyArrayWhenNoAssignmentsExist()
            throws Exception {

        when(service.findAllGrouped())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/transgenic-line-genes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(service).findAllGrouped();
    }

    @Test
    void shouldCreateAssignment() throws Exception {
        TransgenicLineGeneRowResponse response =
                new TransgenicLineGeneRowResponse(
                        10L,
                        "OT-I",
                        1L,
                        "C57BL6",
                        "C57BL/6",
                        List.of(
                                new GeneResponse(
                                        100L,
                                        "OT1",
                                        "Ovalbumin-specific T-cell receptor"
                                )
                        )
                );

        when(service.create(
                any(TransgenicLineGeneRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/transgenic-line-genes")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "transgenicLineId": 10,
                                          "geneId": 100
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transgenicLineId")
                        .value(10))
                .andExpect(jsonPath("$.transgenicLineName")
                        .value("OT-I"))
                .andExpect(jsonPath("$.strainCode")
                        .value("C57BL6"))
                .andExpect(jsonPath("$.genes.length()")
                        .value(1))
                .andExpect(jsonPath("$.genes[0].id")
                        .value(100))
                .andExpect(jsonPath("$.genes[0].symbol")
                        .value("OT1"));

        verify(service)
                .create(any(TransgenicLineGeneRequest.class));
    }

    @Test
    void shouldRejectCreateRequestWithoutTransgenicLineId()
            throws Exception {

        mockMvc.perform(
                        post("/api/transgenic-line-genes")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "transgenicLineId": null,
                                          "geneId": 100
                                        }
                                        """))
                .andExpect(status().isBadRequest());

        verify(service, never())
                .create(any(TransgenicLineGeneRequest.class));
    }

    @Test
    void shouldRejectCreateRequestWithoutGeneId()
            throws Exception {

        mockMvc.perform(
                        post("/api/transgenic-line-genes")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "transgenicLineId": 10,
                                          "geneId": null
                                        }
                                        """))
                .andExpect(status().isBadRequest());

        verify(service, never())
                .create(any(TransgenicLineGeneRequest.class));
    }

    @Test
    void shouldUpdateAssignment() throws Exception {
        Long currentLineId = 10L;
        Long currentGeneId = 100L;

        TransgenicLineGeneRowResponse response =
                new TransgenicLineGeneRowResponse(
                        20L,
                        "OT-II",
                        1L,
                        "C57BL6",
                        "C57BL/6",
                        List.of(
                                new GeneResponse(
                                        200L,
                                        "GFP",
                                        "Green fluorescent protein"
                                )
                        )
                );

        when(service.update(
                eq(currentLineId),
                eq(currentGeneId),
                any(TransgenicLineGeneRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put(
                                "/api/transgenic-line-genes/{transgenicLineId}/{geneId}",
                                currentLineId,
                                currentGeneId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "transgenicLineId": 20,
                                          "geneId": 200
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transgenicLineId")
                        .value(20))
                .andExpect(jsonPath("$.transgenicLineName")
                        .value("OT-II"))
                .andExpect(jsonPath("$.genes.length()")
                        .value(1))
                .andExpect(jsonPath("$.genes[0].id")
                        .value(200))
                .andExpect(jsonPath("$.genes[0].symbol")
                        .value("GFP"));

        verify(service).update(
                eq(currentLineId),
                eq(currentGeneId),
                any(TransgenicLineGeneRequest.class)
        );
    }

    @Test
    void shouldRejectInvalidUpdateRequest()
            throws Exception {

        Long currentLineId = 10L;
        Long currentGeneId = 100L;

        mockMvc.perform(
                        put(
                                "/api/transgenic-line-genes/{transgenicLineId}/{geneId}",
                                currentLineId,
                                currentGeneId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "transgenicLineId": null,
                                          "geneId": null
                                        }
                                        """))
                .andExpect(status().isBadRequest());

        verify(service, never()).update(
                eq(currentLineId),
                eq(currentGeneId),
                any(TransgenicLineGeneRequest.class)
        );
    }

    @Test
    void shouldReturnNotFoundForTransgenicLineGeneNotFoundExceptionWhenUpdatingMissingAssignment()
            throws Exception {

        Long currentLineId = 10L;
        Long currentGeneId = 100L;

        TransgenicLineGeneId currentId =
                new TransgenicLineGeneId(
                        currentLineId,
                        currentGeneId
                );

        when(service.update(
                eq(currentLineId),
                eq(currentGeneId),
                any(TransgenicLineGeneRequest.class)))
                .thenThrow(
                        new TransgenicLineGeneNotFoundException(
                                currentId
                        )
                );

        mockMvc.perform(
                        put(
                                "/api/transgenic-line-genes/{transgenicLineId}/{geneId}",
                                currentLineId,
                                currentGeneId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "transgenicLineId": 20,
                                          "geneId": 200
                                        }
                                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(containsString("TransgenicLine_Gene assignment does not exist:")));

        verify(service).update(
                eq(currentLineId),
                eq(currentGeneId),
                any(TransgenicLineGeneRequest.class)
        );
    }
    
    @Test
    void shouldDeleteExistingAssignment()
            throws Exception {

        Long lineId = 10L;
        Long geneId = 100L;

        when(service.delete(lineId, geneId))
                .thenReturn(true);

        mockMvc.perform(
                        delete(
                                "/api/transgenic-line-genes/{transgenicLineId}/{geneId}",
                                lineId,
                                geneId
                        ))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).delete(lineId, geneId);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingAssignment()
            throws Exception {

        Long lineId = 10L;
        Long geneId = 999L;

        when(service.delete(lineId, geneId))
                .thenReturn(false);

        mockMvc.perform(
                        delete(
                                "/api/transgenic-line-genes/{transgenicLineId}/{geneId}",
                                lineId,
                                geneId
                        ))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));

        verify(service).delete(lineId, geneId);
    }
}