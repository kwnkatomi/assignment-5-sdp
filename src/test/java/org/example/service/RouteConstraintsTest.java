package org.example.service;

import org.example.exception.NoSuitableRouteException;
import org.example.model.*;
import org.example.strategy.CheapestRouteStrategy;
import org.example.strategy.FastestRouteStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteConstraintsTest {

    private final Location origin =
            new Location("Warehouse", 51.16, 71.43);

    private final Location destination =
            new Location("Customer", 51.18, 71.46);

    private Route route(
            String id,
            int minutes,
            double baseCost,
            int trafficDelay,
            boolean available
    ) {
        return new Route(
                id, origin, destination,
                1, minutes, baseCost, 0, 1,
                trafficDelay, available
        );
    }

    @Test
    void rejectsCheapRouteWhenTrafficCausesDeadlineViolation() {
        Route delayed = route("DELAYED", 15, 0, 10, true);
        Route punctual = route("PUNCTUAL", 18, 100, 0, true);

        var request = new DeliveryRequest(
                DeliveryPriority.ECONOMY, 1000, 20, false
        );

        var decision = new CheapestRouteStrategy()
                .selectRoute(List.of(delayed, punctual), request);

        assertEquals("PUNCTUAL", decision.route().id());

        assertTrue(
                decision.rejectedRoutes().stream()
                        .anyMatch(reason ->
                                reason.contains("DELAYED")
                                        && reason.contains("exceeds deadline")
                        )
        );
    }

    @Test
    void unavailableRouteCannotWinEvenWhenItIsFastest() {
        Route closed = route("CLOSED", 5, 50, 0, false);
        Route open = route("OPEN", 20, 50, 0, true);

        var request = new DeliveryRequest(
                DeliveryPriority.STANDARD, 1000, 60, false
        );

        var decision = new FastestRouteStrategy()
                .selectRoute(List.of(closed, open), request);

        assertEquals("OPEN", decision.route().id());

        assertTrue(
                decision.rejectedRoutes().stream()
                        .anyMatch(reason ->
                                reason.contains("CLOSED")
                                        && reason.contains("unavailable")
                        )
        );
    }

    @Test
    void acceptsRouteExactlyAtBudgetAndDeadline() {
        Route boundary = route("BOUNDARY", 20, 50, 5, true);

        var request = new DeliveryRequest(
                DeliveryPriority.STANDARD, 100, 25, false
        );

        var decision = new FastestRouteStrategy()
                .selectRoute(List.of(boundary), request);

        assertEquals("BOUNDARY", decision.route().id());
        assertEquals(25.0, decision.score());
        assertTrue(decision.rejectedRoutes().isEmpty());
    }

    @Test
    void emptyRouteListThrowsExplicitException() {
        var request = new DeliveryRequest(
                DeliveryPriority.STANDARD, 1000, 60, false
        );

        var exception = assertThrows(
                NoSuitableRouteException.class,
                () -> new FastestRouteStrategy()
                        .selectRoute(List.of(), request)
        );

        assertTrue(
                exception.getRejectionReasons().stream()
                        .anyMatch(reason -> reason.contains("empty"))
        );
    }

    @Test
    void noValidRoutePreservesAllConstraintFailureReasons() {
        Route unsuitable = route("INVALID", 20, 50, 0, true);

        var request = new DeliveryRequest(
                DeliveryPriority.STANDARD, 1, 5, false
        );

        var exception = assertThrows(
                NoSuitableRouteException.class,
                () -> new FastestRouteStrategy()
                        .selectRoute(List.of(unsuitable), request)
        );

        String reasons = String.join(
                "; ", exception.getRejectionReasons()
        );

        assertTrue(reasons.contains("INVALID"));
        assertTrue(reasons.contains("exceeds budget"));
        assertTrue(reasons.contains("exceeds deadline"));
    }

    @Test
    void rejectsNegativeDistanceAndNegativeCost() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new Route(
                                "BAD-DISTANCE", origin, destination,
                                -1, 20, 50, 0, 1, 0, true
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> route("BAD-COST", 20, -50, 0, true)
                )
        );
    }

    @Test
    void missingRequestIsRejectedExplicitly() {
        Route valid = route("VALID", 20, 50, 0, true);

        assertThrows(
                NullPointerException.class,
                () -> new FastestRouteStrategy()
                        .selectRoute(List.of(valid), null)
        );
    }
}