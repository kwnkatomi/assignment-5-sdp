package org.example.service;

import org.example.exception.NoSuitableRouteException;
import org.example.model.DeliveryPriority;
import org.example.model.DeliveryRequest;
import org.example.model.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RouteEvaluator {

    private static final double COST_PER_KM = 50.0;
    private static final double URGENT_SURCHARGE_RATE = 0.20;

    private RouteEvaluator() {
    }

    public static long deliveryMinutes(Route route) {
        return (long) route.estimatedMinutes() + route.trafficDelayMinutes();
    }

    public static double totalCost(Route route, DeliveryRequest request) {
        double surcharge = request.priority() == DeliveryPriority.URGENT
                ? route.baseCost() * URGENT_SURCHARGE_RATE
                : 0.0;

        return route.baseCost()
                + route.distanceKm() * COST_PER_KM
                + route.additionalFees()
                + surcharge;
    }

    public static Evaluation evaluate(
            List<Route> routes,
            DeliveryRequest request
    ) {
        Objects.requireNonNull(routes, "Route list is required");
        Objects.requireNonNull(request, "Delivery request is required");

        List<Candidate> candidates = new ArrayList<>();
        List<String> rejections = new ArrayList<>();

        for (Route route : routes) {
            Objects.requireNonNull(route, "Route list cannot contain null");

            long minutes = deliveryMinutes(route);
            double cost = totalCost(route, request);
            List<String> reasons = new ArrayList<>();

            if (!route.available()) {
                reasons.add("route is unavailable");
            }

            if (!Double.isFinite(cost) || cost > request.maxBudget()) {
                reasons.add(
                        "total cost " + cost
                                + " exceeds budget " + request.maxBudget()
                );
            }

            if (minutes > request.maxDeliveryMinutes()) {
                reasons.add(
                        "delivery time " + minutes
                                + " exceeds deadline "
                                + request.maxDeliveryMinutes()
                );
            }

            if (reasons.isEmpty()) {
                candidates.add(new Candidate(route, minutes, cost));
            } else {
                rejections.add(
                        route.id() + ": " + String.join("; ", reasons)
                );
            }
        }

        if (candidates.isEmpty()) {
            if (routes.isEmpty()) {
                rejections.add("The candidate route list is empty");
            }

            throw new NoSuitableRouteException(rejections);
        }

        return new Evaluation(candidates, rejections);
    }

    public record Candidate(
            Route route,
            long deliveryMinutes,
            double totalCost
    ) {
    }

    public record Evaluation(
            List<Candidate> candidates,
            List<String> rejectedRoutes
    ) {
        public Evaluation {
            candidates = List.copyOf(candidates);
            rejectedRoutes = List.copyOf(rejectedRoutes);
        }
    }
}