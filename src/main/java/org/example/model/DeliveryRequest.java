package org.example.model;

import java.util.Objects;

public record DeliveryRequest(
        DeliveryPriority priority,
        double maxBudget,
        int maxDeliveryMinutes,
        boolean ecoPreferred
) {
    public DeliveryRequest {
        Objects.requireNonNull(priority, "Delivery priority is required");

        if (!Double.isFinite(maxBudget) || maxBudget < 0) {
            throw new IllegalArgumentException(
                    "Maximum budget must be finite and non-negative"
            );
        }

        if (maxDeliveryMinutes <= 0) {
            throw new IllegalArgumentException(
                    "Maximum delivery time must be positive"
            );
        }
    }
}