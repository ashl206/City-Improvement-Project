package com.example.demo;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RService {
    private static final double INTERVAL_METERS = 500;
    private static final int MAX_POINTS = 20;

    private final OsmClient osmClient;
    private final RClient rClient;
    private final Map<String, List<Roads>> cache = new ConcurrentHashMap<>();

    public RService(OsmClient osmClient, RClient rClient) {
        this.osmClient = osmClient;
        this.rClient = rClient;
    }

    public List<Roads> analyze(RRequest request) {
        String key = String.format(Locale.US, "%.5f,%.5f,%.5f,%.5f",
                request.stLat(), request.stLng(),
                request.endLat(), request.endLng());

        List<Roads> cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        List<Areas> path = rClient.getPath(
                request.stLat(), request.stLng(), request.endLat(), request.endLng());

        List<Areas> points = path.isEmpty()
                ? RouteSampler.sample(request.stLat(), request.stLng(),
                        request.endLat(), request.endLng(), INTERVAL_METERS) // fallback: straight line
                : RouteSampler.sampleAlong(path, INTERVAL_METERS);

        if (points.size() > MAX_POINTS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Route too long. Max " + MAX_POINTS + " points.");
        }

        List<Roads> result = osmClient.getOsmData(points);

        // Only cache results with at least one real data point,
        // so a busy Overpass server doesn't poison the cache
        boolean hasRealData = result.stream()
                .anyMatch(r -> r.streetType() != Highway.UNKNOWN);
        if (hasRealData) {
            cache.put(key, result);
        }

        return result;
    }
}