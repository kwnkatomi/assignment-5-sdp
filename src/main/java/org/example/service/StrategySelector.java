package org.example.service;

import org.example.model.DeliveryPriority;
import org.example.model.DeliveryRequest;
import org.example.strategy.*;

import java.util.Map;
import java.util.Objects;

public class StrategySelector {

    private final Map<DeliveryPriority, RouteStrategy> strategies;

    public StrategySelector() {
        this(Map.of(
                DeliveryPriority.URGENT, new FastestRouteStrategy(),
                DeliveryPriority.ECONOMY, new CheapestRouteStrategy(),
                DeliveryPriority.ECO, new EcoFriendlyRouteStrategy(),
                DeliveryPriority.STANDARD,
                new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2)
        ));
    }

    public StrategySelector(
            Map<DeliveryPriority, RouteStrategy> strategies
    ) {
        this.strategies = Map.copyOf(
                Objects.requireNonNull(
                        strategies,
                        "Strategy mapping is required"
                )
        );
    }

    public RouteStrategy select(DeliveryRequest request) {
        Objects.requireNonNull(request, "Delivery request is required");

        DeliveryPriority priority = request.priority();

        if (priority != DeliveryPriority.URGENT && request.ecoPreferred()) {
            priority = DeliveryPriority.ECO;
        }

        RouteStrategy strategy = strategies.get(priority);

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "No strategy configured for priority: " + priority
            );
        }

        return strategy;
    }
}