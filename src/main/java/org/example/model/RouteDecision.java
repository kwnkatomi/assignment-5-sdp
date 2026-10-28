package org.example.model;

import java.util.List;
import java.util.Objects;

public record RouteDecision(
        Route route,
        String strategyName,
        double score,
        String explanation,
        List<String> rejectedRoutes
) {
    public RouteDecision {
        Objects.requireNonNull(route, "Selected route is required");

        if (strategyName == null || strategyName.isBlank()) {
            throw new IllegalArgumentException("Strategy name is required");
        }

        if (!Double.isFinite(score)) {
            throw new IllegalArgumentException("Score must be finite");
        }

        if (explanation == null || explanation.isBlank()) {
            throw new IllegalArgumentException("Explanation is required");
        }

        rejectedRoutes = List.copyOf(
                Objects.requireNonNull(
                        rejectedRoutes,
                        "Rejected route explanations are required"
                )
        );
    }
}