package org.example.strategy;

import org.example.model.DeliveryPriority;
import org.example.model.DeliveryRequest;
import org.example.model.Location;
import org.example.model.Route;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FastestRouteStrategyTest {

    private final Location origin =
            new Location("Warehouse", 51.16, 71.43);

    private final Location destination =
            new Location("Customer", 51.18, 71.46);

    @Test
    void considersTrafficInsteadOfOnlyEstimatedTime() {
        Route congested = new Route(
                "A", origin, destination,
                1, 10, 50, 0, 1, 20, true
        );

        Route clear = new Route(
                "B", origin, destination,
                1, 20, 50, 0, 1, 0, true
        );

        var request = new DeliveryRequest(
                DeliveryPriority.STANDARD, 1000, 60, false
        );

        var decision = new FastestRouteStrategy()
                .selectRoute(List.of(congested, clear), request);

        assertEquals("B", decision.route().id());
        assertEquals(20.0, decision.score());
        assertTrue(decision.explanation().contains("traffic delay"));
    }

    @Test
    void rejectsFasterRouteWhenTotalCostExceedsBudget() {
        Route expensive = new Route(
                "A", origin, destination,
                1, 10, 200, 0, 1, 0, true
        );

        Route affordable = new Route(
                "B", origin, destination,
                1, 20, 50, 0, 1, 0, true
        );

        var request = new DeliveryRequest(
                DeliveryPriority.STANDARD, 100, 60, false
        );

        var decision = new FastestRouteStrategy()
                .selectRoute(List.of(expensive, affordable), request);

        assertEquals("B", decision.route().id());
        assertEquals(1, decision.rejectedRoutes().size());
        assertTrue(decision.rejectedRoutes().get(0).contains("exceeds budget"));
    }
}