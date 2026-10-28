package org.example.model;

import java.util.Objects;

public record Route(
        String id,
        Location origin,
        Location destination,
        double distanceKm,
        int estimatedMinutes,
        double baseCost,
        double additionalFees,
        double carbonEmission,
        int trafficDelayMinutes,
        boolean available
) {
    public Route {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Route ID is required");
        }

        Objects.requireNonNull(origin, "Origin is required");
        Objects.requireNonNull(destination, "Destination is required");

        if (!Double.isFinite(distanceKm) || distanceKm < 0) {
            throw new IllegalArgumentException(
                    "Distance must be finite and non-negative"
            );
        }

        if (estimatedMinutes <= 0 || trafficDelayMinutes < 0) {
            throw new IllegalArgumentException(
                    "Estimated time must be positive and traffic delay non-negative"
            );
        }

        if (!Double.isFinite(baseCost) || baseCost < 0
                || !Double.isFinite(additionalFees) || additionalFees < 0) {
            throw new IllegalArgumentException(
                    "Costs must be finite and non-negative"
            );
        }

        if (!Double.isFinite(carbonEmission) || carbonEmission < 0) {
            throw new IllegalArgumentException(
                    "Emissions must be finite and non-negative"
            );
        }
    }
}