package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;

@Service
public class RService {
    private static final double INTERVAL_METERS = 500;
    private static final int MAX_POINTS = 20;
    private static final File CACHE_FILE = new File("demo-cache.json");
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final boolean demoOnly;

    private final OsmClient osmClient;
    private final RClient rClient;
    private final AiClient aiClient;
    private final ImageClient imClient;
    private final Map<String, List<Roads>> cache = new ConcurrentHashMap<>();

    public RService(OsmClient osmClient, RClient rClient, AiClient aiClient, ImageClient imClient,
            @Value("${demo.only:false}") boolean demoOnly) {
        this.osmClient = osmClient;
        this.rClient = rClient;
        this.aiClient = aiClient;
        this.imClient = imClient;
        this.demoOnly = demoOnly;

        try {
            if (CACHE_FILE.exists()) {
                Map<String, List<Roads>> saved = mapper.readValue(
                        CACHE_FILE, new TypeReference<Map<String, List<Roads>>>() {
                        });
                cache.putAll(saved);
                System.out.println("Loaded " + saved.size() + " saved routes");
            }
        } catch (Exception e) {
            System.err.println("Could not load demo-cache.json: " + e.getMessage());
        }
    }

    private void saveCache() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(CACHE_FILE, cache);
        } catch (Exception e) {
            System.err.println("Could not save demo-cache.json: " + e.getMessage());
        }
    }

    public List<Roads> analyze(RRequest request) {
        String key = String.format(Locale.US, "%.5f,%.5f,%.5f,%.5f",
                request.stLat(), request.stLng(),
                request.endLat(), request.endLng());

        List<Roads> cached = cache.get(key);
        if (cached != null) {
            return cached.stream().map(ImprovementsRules::apply).toList();
        }

        if (demoOnly) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "This route isn't available in demo mode.");
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

        List<Roads> osm = osmClient.getOsmData(points);

        List<Roads> result = new ArrayList<>();
        for (Roads r : osm) {
            byte[] image = imClient.getImage(r.lat(), r.lng());
            AiResult ai = aiClient.analyze(image);
            result.add(ImprovementsRules.apply(new Roads(r.lat(), r.lng(), r.hasSide(), r.hasBike(), r.hasTree(),
                    r.hasGreen(), r.streetType(), ai)));
        }

        // Only cache results with at least one real data point,
        // so a busy Overpass server doesn't poison the cache
        boolean hasRealData = osm.stream()
                .anyMatch(r -> r.streetType() != Highway.UNKNOWN);
        if (hasRealData) {
            cache.put(key, result);
            saveCache();
        }

        return result;
    }
}