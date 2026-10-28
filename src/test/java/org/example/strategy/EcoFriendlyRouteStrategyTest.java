package org.example.strategy;

import org.example.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EcoFriendlyRouteStrategyTest {

    @Test
    void selectsLowestEnvironmentalScoreRatherThanShortestDistance() {
        Location origin = new Location("Warehouse", 51.16, 71.43);
        Location destination = new Location("Customer", 51.18, 71.46);

        Route cleaner = new Route(
                "A", origin, destination,
                10, 25, 100, 0, 1, 0, true
        );

        Route shorter = new Route(
                "B", origin, destination,
                2, 20, 100, 0, 2, 0, true
        );

        var request = new DeliveryRequest(
                DeliveryPriority.ECO, 1000, 60, true
        );

        var decision = new EcoFriendlyRouteStrategy()
                .selectRoute(List.of(cleaner, shorter), request);

        assertEquals("A", decision.route().id());
        assertEquals(15.0, decision.score(), 0.001);
    }
}