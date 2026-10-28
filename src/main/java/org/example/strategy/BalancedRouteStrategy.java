package org.example.strategy;

import org.example.model.DeliveryRequest;
import org.example.model.Route;
import org.example.model.RouteDecision;
import org.example.service.RouteEvaluator;
import org.example.service.RouteEvaluator.Candidate;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

public class BalancedRouteStrategy implements RouteStrategy {

    private final double timeWeight;
    private final double costWeight;
    private final double distanceWeight;
    private final double emissionWeight;

    public BalancedRouteStrategy(
            double timeWeight,
            double costWeight,
            double distanceWeight,
            double emissionWeight
    ) {
        double[] weights = {
                timeWeight, costWeight, distanceWeight, emissionWeight
        };

        double sum = 0;

        for (double weight : weights) {
            if (!Double.isFinite(weight) || weight < 0) {
                throw new IllegalArgumentException(
                        "Weights must be finite and non-negative"
                );
            }

            sum += weight;
        }

        if (Math.abs(sum - 1.0) > 1e-9) {
            throw new IllegalArgumentException("Weights must sum to 1.0");
        }

        this.timeWeight = timeWeight;
        this.costWeight = costWeight;
        this.distanceWeight = distanceWeight;
        this.emissionWeight = emissionWeight;
    }

    @Override
    public RouteDecision selectRoute(
            List<Route> routes,
            DeliveryRequest request
    ) {
        var evaluation = RouteEvaluator.evaluate(routes, request);
        List<Candidate> candidates = evaluation.candidates();

        Range time = range(candidates, Candidate::deliveryMinutes);
        Range cost = range(candidates, Candidate::totalCost);

        Range distance = range(
                candidates,
                candidate -> candidate.route().distanceKm()
        );

        Range emission = range(
                candidates,
                candidate -> candidate.route().carbonEmission()
        );

        ToDoubleFunction<Candidate> scorer = candidate ->
                time.normalize(candidate.deliveryMinutes()) * timeWeight
                        + cost.normalize(candidate.totalCost()) * costWeight
                        + distance.normalize(candidate.route().distanceKm())
                        * distanceWeight
                        + emission.normalize(candidate.route().carbonEmission())
                        * emissionWeight;

        Candidate best = candidates.stream()
                .min(
                        Comparator.comparingDouble(scorer)
                                .thenComparing(candidate -> candidate.route().id())
                )
                .orElseThrow();

        double score = scorer.applyAsDouble(best);

        String explanation = String.format(
                Locale.ROOT,
                "Lowest weighted normalized score among feasible routes. "
                        + "Time: %.4f x %.2f; cost: %.4f x %.2f; "
                        + "distance: %.4f x %.2f; emissions: %.4f x %.2f. "
                        + "Final score: %.4f.",
                time.normalize(best.deliveryMinutes()), timeWeight,
                cost.normalize(best.totalCost()), costWeight,
                distance.normalize(best.route().distanceKm()), distanceWeight,
                emission.normalize(best.route().carbonEmission()), emissionWeight,
                score
        );

        return new RouteDecision(
                best.route(),
                getStrategyName(),
                score,
                explanation,
                evaluation.rejectedRoutes()
        );
    }

    private static Range range(
            List<Candidate> candidates,
            ToDoubleFunction<Candidate> metric
    ) {
        double min = candidates.stream()
                .mapToDouble(metric)
                .min()
                .orElseThrow();

        double max = candidates.stream()
                .mapToDouble(metric)
                .max()
                .orElseThrow();

        return new Range(min, max);
    }

    private record Range(double min, double max) {

        double normalize(double value) {
            return max == min ? 0.0 : (value - min) / (max - min);
        }
    }

    @Override
    public String getStrategyName() {
        return "Balanced";
    }
}