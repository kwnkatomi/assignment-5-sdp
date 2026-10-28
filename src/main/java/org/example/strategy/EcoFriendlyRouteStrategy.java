package org.example.strategy;

import org.example.model.DeliveryRequest;
import org.example.model.Route;
import org.example.model.RouteDecision;
import org.example.service.RouteEvaluator;
import org.example.service.RouteEvaluator.Candidate;

import java.util.Comparator;
import java.util.List;

public class EcoFriendlyRouteStrategy implements RouteStrategy {

    private static final double EMISSION_WEIGHT = 10.0;
    private static final double DISTANCE_WEIGHT = 0.5;

    private double ecoScore(Candidate candidate) {
        return candidate.route().carbonEmission() * EMISSION_WEIGHT
                + candidate.route().distanceKm() * DISTANCE_WEIGHT;
    }

    @Override
    public RouteDecision selectRoute(
            List<Route> routes,
            DeliveryRequest request
    ) {
        var evaluation = RouteEvaluator.evaluate(routes, request);

        Candidate best = evaluation.candidates().stream()
                .min(
                        Comparator.comparingDouble(this::ecoScore)
                                .thenComparing(candidate -> candidate.route().id())
                )
                .orElseThrow();

        Route route = best.route();
        double score = ecoScore(best);

        String explanation =
                "Lowest environmental score among feasible routes. "
                        + "CO2: " + route.carbonEmission()
                        + " kg; distance: " + route.distanceKm()
                        + " km; score = CO2 x 10 + distance x 0.5 = "
                        + score + ".";

        return new RouteDecision(
                route,
                getStrategyName(),
                score,
                explanation,
                evaluation.rejectedRoutes()
        );
    }

    @Override
    public String getStrategyName() {
        return "Eco-friendly";
    }
}