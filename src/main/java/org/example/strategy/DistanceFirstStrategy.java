package org.example.strategy;

import org.example.model.DeliveryRequest;
import org.example.model.Route;
import org.example.model.RouteDecision;
import org.example.service.RouteEvaluator;
import org.example.service.RouteEvaluator.Candidate;

import java.util.Comparator;
import java.util.List;

public class DistanceFirstStrategy implements RouteStrategy {

    @Override
    public RouteDecision selectRoute(
            List<Route> routes,
            DeliveryRequest request
    ) {
        var evaluation = RouteEvaluator.evaluate(routes, request);

        Candidate best = evaluation.candidates().stream()
                .min(
                        Comparator
                                .comparingDouble(
                                        (Candidate candidate) ->
                                                candidate.route().distanceKm()
                                )
                                .thenComparing(candidate -> candidate.route().id())
                )
                .orElseThrow();

        Route route = best.route();

        String explanation =
                "Shortest distance among feasible routes. "
                        + "Distance: " + route.distanceKm()
                        + " km; delivery time: " + best.deliveryMinutes()
                        + " min; total cost: " + best.totalCost() + " KZT.";

        return new RouteDecision(
                route,
                getStrategyName(),
                route.distanceKm(),
                explanation,
                evaluation.rejectedRoutes()
        );
    }

    @Override
    public String getStrategyName() {
        return "Distance-first";
    }
}