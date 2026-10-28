package org.example.service;

import org.example.model.*;
import org.example.strategy.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StrategyComparisonTest {

    @Test
    void comparesTwoStrategiesUsingTheSameRequest() {
        Location origin = new Location("Warehouse", 51.16, 71.43);
        Location destination = new Location("Customer", 51.18, 71.46);

        List<Route> routes = List.of(
                new Route(
                        "FAST", origin, destination,
                        10, 10, 100, 0, 3, 0, true
                ),
                new Route(
                        "CHEAP", origin, destination,
                        2, 20, 200, 0, 1, 0, true
                )
        );

        var request = new DeliveryRequest(
                DeliveryPriority.STANDARD, 5000, 60, false
        );

        var comparison = new StrategyComparison(List.of(
                new FastestRouteStrategy(),
                new CheapestRouteStrategy()
        ));

        var decisions = comparison.compare(routes, request);

        assertEquals(2, decisions.size());

        assertEquals("Fastest", decisions.get(0).strategyName());
        assertEquals("FAST", decisions.get(0).route().id());

        assertEquals("Cheapest", decisions.get(1).strategyName());
        assertEquals("CHEAP", decisions.get(1).route().id());
    }
}