package org.example.strategy;

import org.example.model.DeliveryRequest;
import org.example.model.Route;
import org.example.model.RouteDecision;
import org.example.service.RouteEvaluator;
import org.example.service.RouteEvaluator.Candidate;

import java.util.Comparator;
import java.util.List;

public class CheapestRouteStrategy implements RouteStrategy {

    @Override
    public RouteDecision selectRoute(
            List<Route> routes,
            DeliveryRequest request
    ) {
        var evaluation = RouteEvaluator.evaluate(routes, request);

        Candidate best = evaluation.candidates().stream()
                .min(
                        Comparator.comparingDouble(Candidate::totalCost)
                                .thenComparing(candidate -> candidate.route().id())
                )
                .orElseThrow();

        Route route = best.route();

        String explanation =
                "Lowest total cost among feasible routes. "
                        + "Base cost: " + route.baseCost()
                        + "; distance charge: " + route.distanceKm() + " x 50"
                        + "; additional fees: " + route.additionalFees()
                        + "; urgent surcharge: "
                        + (request.priority()
                        == org.example.model.DeliveryPriority.URGENT
                        ? route.baseCost() * 0.20 : 0.0)
                        + "; total cost: " + best.totalCost() + " KZT.";

        return new RouteDecision(
                route,
                getStrategyName(),
                best.totalCost(),
                explanation,
                evaluation.rejectedRoutes()
        );
    }

    @Override
    public String getStrategyName() {
        return "Cheapest";
    }
}