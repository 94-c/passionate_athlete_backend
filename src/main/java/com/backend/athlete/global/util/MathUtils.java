package com.backend.athlete.global.util;

public class MathUtils {
    public MathUtils() {
    }
    public static double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
