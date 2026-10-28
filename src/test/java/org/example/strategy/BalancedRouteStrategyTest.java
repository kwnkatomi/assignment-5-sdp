package org.example.strategy;

import org.example.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BalancedRouteStrategyTest {

    private final Location origin =
            new Location("Warehouse", 51.16, 71.43);

    private final Location destination =
            new Location("Customer", 51.18, 71.46);

    private final DeliveryRequest request = new DeliveryRequest(
            DeliveryPriority.STANDARD, 5000, 120, false
    );

    private List<Route> routes() {
        return List.of(
                new Route(
                        "FAST", origin, destination,
                        10, 10, 500, 0, 10, 0, true
                ),
                new Route(
                        "COMPROMISE", origin, destination,
                        3, 14, 150, 0, 3, 0, true
                ),
                new Route(
                        "CHEAP", origin, destination,
                        1, 30, 50, 0, 1, 0, true
                )
        );
    }

    @Test
    void selectsCompromiseUsingNormalizedMetrics() {
        var strategy = new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2);

        var decision = strategy.selectRoute(routes(), request);

        assertEquals("COMPROMISE", decision.route().id());

        double expected = 0.2 * 0.4 + (2.0 / 9.0) * 0.6;

        assertEquals(expected, decision.score(), 0.000001);
    }

    @Test
    void configurableWeightsCanPrioritizeOnlyTime() {
        var strategy = new BalancedRouteStrategy(1.0, 0.0, 0.0, 0.0);

        var decision = strategy.selectRoute(routes(), request);

        assertEquals("FAST", decision.route().id());
        assertEquals(0.0, decision.score(), 0.000001);
    }

    @Test
    void handlesSingleCandidateWithoutDivisionByZero() {
        var strategy = new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2);
        Route onlyRoute = routes().get(0);

        var decision = strategy.selectRoute(List.of(onlyRoute), request);

        assertEquals(onlyRoute, decision.route());
        assertEquals(0.0, decision.score(), 0.000001);
    }

    @Test
    void rejectsWeightsThatDoNotSumToOne() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BalancedRouteStrategy(0.4, 0.4, 0.4, 0.4)
        );
    }

    @Test
    void rejectsNegativeAndNonFiniteWeights() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new BalancedRouteStrategy(-0.1, 0.5, 0.3, 0.3)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new BalancedRouteStrategy(
                                Double.NaN, 0.3, 0.1, 0.2
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new BalancedRouteStrategy(
                                Double.POSITIVE_INFINITY, 0, 0, 0
                        )
                )
        );
    }
}