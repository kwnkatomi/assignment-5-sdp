package org.example.service;

import org.example.model.DeliveryRequest;
import org.example.model.Route;
import org.example.model.RouteDecision;
import org.example.strategy.RouteStrategy;

import java.util.List;
import java.util.Objects;

public class StrategyComparison {

    private final List<RouteStrategy> strategies;

    public StrategyComparison(List<RouteStrategy> strategies) {
        this.strategies = List.copyOf(
                Objects.requireNonNull(strategies, "Strategies are required")
        );

        if (this.strategies.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one strategy is required"
            );
        }
    }

    public List<RouteDecision> compare(
            List<Route> routes,
            DeliveryRequest request
    ) {
        Objects.requireNonNull(routes, "Routes are required");
        Objects.requireNonNull(request, "Request is required");

        return strategies.stream()
                .map(strategy -> strategy.selectRoute(routes, request))
                .toList();
    }
}