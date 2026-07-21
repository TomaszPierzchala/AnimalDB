package cz.animalhouse.controller;

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

import cz.animalhouse.dto.TransgenicLineRequest;
import cz.animalhouse.dto.TransgenicLineResponse;
import cz.animalhouse.service.TransgenicLineService;

@WebMvcTest(TransgenicLineController.class)
class TransgenicLineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransgenicLineService transgenicLineService;

    @Test
    void shouldReturnAllTransgenicLines() throws Exception {

        TransgenicLineResponse line1 =
                new TransgenicLineResponse(
                        10L,
                        1L,
                        "C57BL6",
                        "C57BL/6",
                        "OT-I");

        TransgenicLineResponse line2 =
                new TransgenicLineResponse(
                        11L,
                        1L,
                        "C57BL6_2",
                        "C57BL/6/2",
                        "OT-II");

        when(transgenicLineService.findAll())
                .thenReturn(List.of(line1, line2));

        mockMvc.perform(
                        get("/api/transgenic-lines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("OT-I"))
                .andExpect(jsonPath("$[1].name").value("OT-II"))
                .andExpect(jsonPath("$[0].strainId").value(1))
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[0].strainCode").value("C57BL6"))
                .andExpect(jsonPath("$[1].strainName").value("C57BL/6/2"))
                ;
    }

    @Test
    void shouldCreateTransgenicLine() throws Exception {

        TransgenicLineRequest request =
                new TransgenicLineRequest(
                        1L,
                        "OT-I");

        TransgenicLineResponse response =
                new TransgenicLineResponse(
                        10L,
                        1L,
                        "C57BL6",
                        "C57BL/6",
                        "OT-I");

        when(transgenicLineService.create(request))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/transgenic-lines")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "strainId": 1,
                                          "name": "OT-I"
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.strainId").value(1))
                .andExpect(jsonPath("$.strainCode")
                        .value("C57BL6"))
                .andExpect(jsonPath("$.strainName")
                        .value("C57BL/6"))
                .andExpect(jsonPath("$.name")
                        .value("OT-I"));
    }

    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {

        mockMvc.perform(
                        post("/api/transgenic-lines")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "strainId": null,
                                          "name": ""
                                        }
                                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteExistingTransgenicLine()
            throws Exception {

        when(transgenicLineService.delete(10L))
                .thenReturn(true);

        mockMvc.perform(
                        delete("/api/transgenic-lines/10"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingLine()
            throws Exception {

        when(transgenicLineService.delete(999L))
                .thenReturn(false);

        mockMvc.perform(
                        delete("/api/transgenic-lines/999"))
                .andExpect(status().isNotFound());
    }
}