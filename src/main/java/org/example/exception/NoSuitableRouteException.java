package org.example.exception;

import java.util.List;
import java.util.Objects;

public class NoSuitableRouteException extends RuntimeException {

    private final List<String> rejectionReasons;

    public NoSuitableRouteException(List<String> rejectionReasons) {
        super("No route satisfies the delivery constraints");

        this.rejectionReasons = List.copyOf(
                Objects.requireNonNull(
                        rejectionReasons,
                        "Rejection reasons are required"
                )
        );
    }

    public List<String> getRejectionReasons() {
        return rejectionReasons;
    }
}