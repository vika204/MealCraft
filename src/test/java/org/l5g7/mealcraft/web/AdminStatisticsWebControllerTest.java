package org.l5g7.mealcraft.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.l5g7.mealcraft.app.statistics.internal.DailyStats;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminStatisticsWebControllerTest {

    private MockMvc mockMvc;
    private RestClient internalApiClient;

    @BeforeEach
    void setUp() {
        internalApiClient = Mockito.mock(RestClient.class, RETURNS_DEEP_STUBS);
        AdminStatisticsWebController controller = new AdminStatisticsWebController(internalApiClient);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void showStats_noParams_displaysAllStats() throws Exception {
        Mockito.when(internalApiClient.get()
                        .uri("/statistics")
                        .retrieve()
                        .toEntity(Mockito.any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of(new DailyStats())));

        mockMvc.perform(get("/mealcraft/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-page"))
                .andExpect(model().attribute("title", "Statistics"))
                .andExpect(model().attributeExists("data", "fragmentToLoad"));
    }

    @Test
    void showStats_withDay_filtersStats() throws Exception {
        Mockito.when(internalApiClient.get()
                        .uri("/statistics?day={day}", "2025-01-01")
                        .retrieve()
                        .toEntity(Mockito.any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of(new DailyStats())));

        mockMvc.perform(get("/mealcraft/admin/stats").param("day", "2025-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-page"))
                .andExpect(model().attribute("searchDay", "2025-01-01"));
    }

    @Test
    void showStats_withRecalculate_callsRecalculate() throws Exception {
        Mockito.when(internalApiClient.post()
                        .uri("/statistics/recalculate")
                        .retrieve()
                        .toBodilessEntity())
                .thenReturn(ResponseEntity.ok().build());

        Mockito.when(internalApiClient.get()
                        .uri("/statistics")
                        .retrieve()
                        .toEntity(Mockito.any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/mealcraft/admin/stats").param("action", "recalculate"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-page"));
    }

    @Test
    void showStats_withAllAction_displaysAllStats() throws Exception {
        Mockito.when(internalApiClient.get()
                        .uri("/statistics")
                        .retrieve()
                        .toEntity(Mockito.any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/mealcraft/admin/stats").param("action", "all"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-page"));
    }
}

