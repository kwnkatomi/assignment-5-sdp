package org.example.strategy;

import org.example.model.DeliveryRequest;
import org.example.model.Route;
import org.example.model.RouteDecision;
import org.example.service.RouteEvaluator;
import org.example.service.RouteEvaluator.Candidate;

import java.util.Comparator;
import java.util.List;

public class FastestRouteStrategy implements RouteStrategy {

    @Override
    public RouteDecision selectRoute(
            List<Route> routes,
            DeliveryRequest request
    ) {
        var evaluation = RouteEvaluator.evaluate(routes, request);

        Candidate best = evaluation.candidates().stream()
                .min(
                        Comparator.comparingLong(Candidate::deliveryMinutes)
                                .thenComparing(candidate -> candidate.route().id())
                )
                .orElseThrow();

        Route route = best.route();

        String explanation =
                "Lowest traffic-adjusted delivery time among feasible routes. "
                        + "Estimated time: " + route.estimatedMinutes()
                        + " min; traffic delay: +" + route.trafficDelayMinutes()
                        + " min; final time: " + best.deliveryMinutes()
                        + " min; total cost: " + best.totalCost() + " KZT.";

        return new RouteDecision(
                route,
                getStrategyName(),
                best.deliveryMinutes(),
                explanation,
                evaluation.rejectedRoutes()
        );
    }

    @Override
    public String getStrategyName() {
        return "Fastest";
    }
}