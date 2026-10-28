package org.example.strategy;

import org.example.model.*;
import org.example.service.RouteEvaluator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CheapestRouteStrategyTest {

    private final Location origin =
            new Location("Warehouse", 51.16, 71.43);

    private final Location destination =
            new Location("Customer", 51.18, 71.46);

    @Test
    void selectsLowestTotalCostRatherThanLowestBaseCost() {
        Route lowBase = new Route(
                "A", origin, destination,
                10, 20, 100, 0, 1, 0, true
        );

        Route lowTotal = new Route(
                "B", origin, destination,
                2, 25, 200, 0, 1, 0, true
        );

        var request = new DeliveryRequest(
                DeliveryPriority.ECONOMY, 1000, 60, false
        );

        var decision = new CheapestRouteStrategy()
                .selectRoute(List.of(lowBase, lowTotal), request);

        assertEquals("B", decision.route().id());
        assertEquals(300.0, decision.score(), 0.001);
    }

    @Test
    void addsUrgentSurchargeToTotalCost() {
        Route route = new Route(
                "A", origin, destination,
                2, 20, 100, 30, 1, 0, true
        );

        var standard = new DeliveryRequest(
                DeliveryPriority.STANDARD, 1000, 60, false
        );

        var urgent = new DeliveryRequest(
                DeliveryPriority.URGENT, 1000, 60, false
        );

        assertEquals(
                230.0,
                RouteEvaluator.totalCost(route, standard),
                0.001
        );

        assertEquals(
                250.0,
                RouteEvaluator.totalCost(route, urgent),
                0.001
        );
    }
}