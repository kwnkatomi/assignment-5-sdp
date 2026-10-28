package org.example.service;

import org.example.model.*;
import org.example.strategy.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoutePlanningTest {

    private final DeliveryRequest request = new DeliveryRequest(
            DeliveryPriority.STANDARD, 5000, 120, false
    );

    private List<Route> routes() {
        Location origin = new Location("Warehouse", 51.16, 71.43);
        Location destination = new Location("Customer", 51.18, 71.46);

        return List.of(
                new Route(
                        "A", origin, destination,
                        10, 10, 100, 0, 3, 0, true
                ),
                new Route(
                        "B", origin, destination,
                        2, 20, 200, 0, 1, 0, true
                )
        );
    }

    @Test
    void samePlannerSupportsThreeRuntimeSwitches() {
        RoutePlanner planner = new RoutePlanner(
                new FastestRouteStrategy()
        );

        assertEquals("A", planner.plan(routes(), request).route().id());

        planner.setStrategy(new CheapestRouteStrategy());

        var cheapest = planner.plan(routes(), request);
        assertEquals("B", cheapest.route().id());
        assertEquals("Cheapest", cheapest.strategyName());

        planner.setStrategy(new EcoFriendlyRouteStrategy());

        var eco = planner.plan(routes(), request);
        assertEquals("B", eco.route().id());
        assertEquals("Eco-friendly", eco.strategyName());

        planner.setStrategy(
                new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2)
        );

        var balanced = planner.plan(routes(), request);
        assertEquals("B", balanced.route().id());
        assertEquals("Balanced", balanced.strategyName());
    }

    @Test
    void automaticallySelectsStrategyForEachPriority() {
        StrategySelector selector = new StrategySelector();

        assertAll(
                () -> assertInstanceOf(
                        FastestRouteStrategy.class,
                        selector.select(new DeliveryRequest(
                                DeliveryPriority.URGENT, 5000, 120, false
                        ))
                ),
                () -> assertInstanceOf(
                        CheapestRouteStrategy.class,
                        selector.select(new DeliveryRequest(
                                DeliveryPriority.ECONOMY, 5000, 120, false
                        ))
                ),
                () -> assertInstanceOf(
                        EcoFriendlyRouteStrategy.class,
                        selector.select(new DeliveryRequest(
                                DeliveryPriority.ECO, 5000, 120, false
                        ))
                ),
                () -> assertInstanceOf(
                        BalancedRouteStrategy.class,
                        selector.select(request)
                )
        );
    }

    @Test
    void ecoPreferenceOverridesStandardPriority() {
        StrategySelector selector = new StrategySelector();

        var ecoRequest = new DeliveryRequest(
                DeliveryPriority.STANDARD, 5000, 120, true
        );

        assertInstanceOf(
                EcoFriendlyRouteStrategy.class,
                selector.select(ecoRequest)
        );
    }

    @Test
    void urgencyTakesPrecedenceOverEcoPreference() {
        StrategySelector selector = new StrategySelector();

        var urgentRequest = new DeliveryRequest(
                DeliveryPriority.URGENT, 5000, 120, true
        );

        assertInstanceOf(
                FastestRouteStrategy.class,
                selector.select(urgentRequest)
        );
    }
}