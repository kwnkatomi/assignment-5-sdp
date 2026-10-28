package org.example.strategy;

import org.example.DemoData;
import org.example.model.DeliveryPriority;
import org.example.model.DeliveryRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistanceFirstStrategyTest {

    @Test
    void selectsShortestAvailableRoute() {
        var request = new DeliveryRequest(
                DeliveryPriority.STANDARD, 5000, 60, false
        );

        var decision = new DistanceFirstStrategy()
                .selectRoute(DemoData.routes(), request);

        assertEquals("R5-Short", decision.route().id());
        assertEquals(7.0, decision.score(), 0.001);

        assertTrue(
                decision.rejectedRoutes().stream()
                        .anyMatch(reason ->
                                reason.contains("R7-Closed")
                                        && reason.contains("unavailable")
                        )
        );
    }

    @Test
    void tighterDeadlineChangesTheSelectedRoute() {
        var strategy = new DistanceFirstStrategy();

        var relaxed = new DeliveryRequest(
                DeliveryPriority.STANDARD, 5000, 60, false
        );

        var tight = new DeliveryRequest(
                DeliveryPriority.STANDARD, 5000, 25, false
        );

        var relaxedDecision = strategy.selectRoute(
                DemoData.routes(), relaxed
        );

        var tightDecision = strategy.selectRoute(
                DemoData.routes(), tight
        );

        assertEquals("R5-Short", relaxedDecision.route().id());
        assertEquals("R1-Express", tightDecision.route().id());

        assertTrue(
                tightDecision.rejectedRoutes().stream()
                        .anyMatch(reason ->
                                reason.contains("R5-Short")
                                        && reason.contains("exceeds deadline")
                        )
        );
    }
}