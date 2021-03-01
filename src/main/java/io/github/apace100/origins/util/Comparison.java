package io.github.apace100.origins.util;

import java.util.function.BiFunction;

public enum Comparison {

    NONE("", (a, b) -> false),
    EQUAL("==", Double::equals),
    LESS_THAN("<", (a, b) -> a < b),
    GREATER_THAN(">", (a, b) -> a > b),
    LESS_THAN_OR_EQUAL("<=", (a, b) -> a <= b),
    GREATER_THAN_OR_EQUAL(">=", (a, b) -> a >= b),
    NOT_EQUAL("!=", (a, b) -> !a.equals(b));

    private final String comparisonString;
    private final BiFunction<Double, Double, Boolean> comparison;

    private Comparison(String comparisonString, BiFunction<Double, Double, Boolean> comparison) {
        this.comparisonString = comparisonString;
        this.comparison = comparison;
    }

    public boolean compare(double a, double b) {
        return comparison.apply(a, b);
    }

    public String getComparisonString() {
        return comparisonString;
    }

    public static Comparison getFromString(String comparisonString) {
        switch(comparisonString) {
            case "==":
                return EQUAL;
            case "<":
                return LESS_THAN;
            case ">":
                return GREATER_THAN;
            case "<=":
                return LESS_THAN_OR_EQUAL;
            case ">=":
                return GREATER_THAN_OR_EQUAL;
            case "!=":
                return NOT_EQUAL;
        }
        return NONE;
    }
}
