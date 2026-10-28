package org.example;

import org.example.model.Location;
import org.example.model.Route;

import java.util.List;

public final class DemoData {

    private DemoData() {
    }

    public static List<Route> routes() {
        Location origin = new Location("Warehouse", 51.16, 71.43);
        Location destination = new Location("Customer", 51.20, 71.48);

        return List.of(
                new Route(
                        "R1-Express", origin, destination,
                        12, 16, 3000, 500, 4.0, 6, true
                ),
                new Route(
                        "R2-Highway", origin, destination,
                        18, 20, 2000, 400, 4.0, 3, true
                ),
                new Route(
                        "R3-Economy", origin, destination,
                        10, 38, 700, 0, 2.0, 5, true
                ),
                new Route(
                        "R4-Electric", origin, destination,
                        11, 30, 1200, 100, 0.4, 2, true
                ),
                new Route(
                        "R5-Short", origin, destination,
                        7, 28, 1600, 50, 1.2, 10, true
                ),
                new Route(
                        "R6-Ring", origin, destination,
                        15, 26, 1000, 150, 1.8, 1, true
                ),
                new Route(
                        "R7-Closed", origin, destination,
                        6, 12, 500, 0, 0.2, 0, false
                ),
                new Route(
                        "R8-Rural", origin, destination,
                        20, 50, 400, 0, 1.0, 2, true
                )
        );
    }
}