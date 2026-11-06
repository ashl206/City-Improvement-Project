package com.example.demo;

import java.util.ArrayList;
import java.util.List;

public class RouteSampler {

    private static final double EARTH_RADIUS_METERS = 6371000;

    // Haversine formula: distance in meters between two coordinates
    public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(a));
    }

    // Returns points from start to end, spaced at most intervalMeters apart
    public static List<Areas> sample(double startLat, double startLng,
                                      double endLat, double endLng,
                                      double intervalMeters) {
        double total = distanceMeters(startLat, startLng, endLat, endLng);
        int steps = Math.max(1, (int) Math.ceil(total / intervalMeters));

        List<Areas> points = new ArrayList<>();
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;   // goes from 0.0 to 1.0
            points.add(new Areas(
                    startLat + (endLat - startLat) * t,
                    startLng + (endLng - startLng) * t));
        }
        return points;
    }
}