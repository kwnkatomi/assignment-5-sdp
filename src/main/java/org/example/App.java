package org.example;

import org.example.exception.NoSuitableRouteException;
import org.example.model.*;
import org.example.service.*;
import org.example.strategy.*;

import java.util.List;
import java.util.Locale;

public class App {

    public static void main(String[] args) {
        List<Route> routes = DemoData.routes();

        List<RouteStrategy> strategies = List.of(
                new FastestRouteStrategy(),
                new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(),
                new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2)
        );

        DeliveryRequest standard = new DeliveryRequest(
                DeliveryPriority.STANDARD, 5000, 60, false
        );

        System.out.println("RUNTIME STRATEGY SWITCHING");

        RoutePlanner planner = new RoutePlanner(strategies.get(0));
        printDecision(planner.plan(routes, standard), standard);

        for (int i = 1; i < strategies.size(); i++) {
            planner.setStrategy(strategies.get(i));
            printDecision(planner.plan(routes, standard), standard);
        }

        System.out.println("\nCOMPARISON: SAME REQUEST");
        System.out.println("Request: " + standard);

        StrategyComparison comparison = new StrategyComparison(strategies);

        for (RouteDecision decision : comparison.compare(routes, standard)) {
            printDecision(decision, standard);
        }

        System.out.println("\nAUTOMATIC STRATEGY SELECTION");

        StrategySelector selector = new StrategySelector();

        List<DeliveryRequest> requests = List.of(
                new DeliveryRequest(
                        DeliveryPriority.URGENT, 6000, 25, false
                ),
                new DeliveryRequest(
                        DeliveryPriority.ECONOMY, 1500, 60, false
                ),
                new DeliveryRequest(
                        DeliveryPriority.ECO, 2500, 45, true
                ),
                standard
        );

        for (DeliveryRequest request : requests) {
            System.out.println("\nRequest: " + request);
            planner.setStrategy(selector.select(request));
            printDecision(planner.plan(routes, request), request);
        }

        System.out.println("\nNO SUITABLE ROUTE");

        DeliveryRequest impossible = new DeliveryRequest(
                DeliveryPriority.STANDARD, 100, 5, false
        );

        planner.setStrategy(selector.select(impossible));

        try {
            planner.plan(routes, impossible);
        } catch (NoSuitableRouteException exception) {
            System.out.println(exception.getMessage());

            for (String reason : exception.getRejectionReasons()) {
                System.out.println("  " + reason);
            }
        }
    }

    private static void printDecision(
            RouteDecision decision,
            DeliveryRequest request
    ) {
        Route route = decision.route();

        System.out.printf(
                Locale.ROOT,
                "%n%s -> %s%n"
                        + "Time: %d min | Cost: %.2f KZT | "
                        + "Distance: %.1f km | CO2: %.2f kg%n"
                        + "Strategy score: %.4f%n",
                decision.strategyName(),
                route.id(),
                RouteEvaluator.deliveryMinutes(route),
                RouteEvaluator.totalCost(route, request),
                route.distanceKm(),
                route.carbonEmission(),
                decision.score()
        );

        System.out.println("Reason: " + decision.explanation());

        for (String rejection : decision.rejectedRoutes()) {
            System.out.println("Rejected: " + rejection);
        }
    }
}