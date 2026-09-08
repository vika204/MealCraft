package org.l5g7.mealcraft.web;

import org.l5g7.mealcraft.app.statistics.internal.DailyStats;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;

@Controller
@RequestMapping("/mealcraft/admin/stats")
public class AdminStatisticsWebController {

    private final RestClient internalApiClient;

    private static final String FRAGMENT_TO_LOAD = "fragmentToLoad";
    private static final String TITLE = "title";
    private static final String ADMIN_PAGE = "admin-page";

    public AdminStatisticsWebController(@Qualifier("internalApiClient") RestClient internalApiClient) {
        this.internalApiClient = internalApiClient;
    }

    @GetMapping
    public String showStats(@RequestParam(required = false) String day,
                            @RequestParam(required = false) String action,
                            Model model) {

        String effectiveDay = day;

        if ("recalculate".equals(action)) {
            if (effectiveDay == null || effectiveDay.isBlank()) {
                internalApiClient
                        .post()
                        .uri("/statistics/recalculate")
                        .retrieve()
                        .toBodilessEntity();
            } else {
                internalApiClient
                        .post()
                        .uri("/statistics/recalculate?day={day}", effectiveDay)
                        .retrieve()
                        .toBodilessEntity();
            }
        }

        if ("all".equals(action)) {
            effectiveDay = null;
        }

        ResponseEntity<List<DailyStats>> response;

        if (effectiveDay == null || effectiveDay.isBlank()) {
            response = internalApiClient
                    .get()
                    .uri("/statistics")
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<DailyStats>>() {});
        } else {
            response = internalApiClient
                    .get()
                    .uri("/statistics?day={day}", effectiveDay)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<DailyStats>>() {});
        }

        List<DailyStats> data = response.getBody();
        if (data == null) {
            data = List.of();
        }

        model.addAttribute("data", data);
        model.addAttribute(FRAGMENT_TO_LOAD, "fragments/stats :: content");
        model.addAttribute(TITLE, "Statistics");
        model.addAttribute("searchDay", effectiveDay);

        return ADMIN_PAGE;
    }
}