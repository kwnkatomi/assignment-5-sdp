package org.example.strategy;

import org.example.model.DeliveryRequest;
import org.example.model.Route;
import org.example.model.RouteDecision;

import java.util.List;

public interface RouteStrategy {

    RouteDecision selectRoute(
            List<Route> routes,
            DeliveryRequest request
    );

    String getStrategyName();
}