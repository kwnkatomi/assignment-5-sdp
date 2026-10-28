package org.example.service;

import org.example.model.DeliveryRequest;
import org.example.model.Route;
import org.example.model.RouteDecision;
import org.example.strategy.RouteStrategy;

import java.util.List;
import java.util.Objects;

public class RoutePlanner {

    private RouteStrategy strategy;

    public RoutePlanner(RouteStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(RouteStrategy strategy) {
        this.strategy = Objects.requireNonNull(
                strategy,
                "Route strategy is required"
        );
    }

    public RouteDecision plan(
            List<Route> routes,
            DeliveryRequest request
    ) {
        return strategy.selectRoute(routes, request);
    }
}