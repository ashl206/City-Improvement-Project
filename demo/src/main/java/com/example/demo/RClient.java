package com.example.demo;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class RClient {

    private final RestClient restClient = RestClient.builder()
            .defaultHeader("User-Agent", "CityImprovementProject/0.1 (contact: youractualemail@gmail.com)")
            .defaultHeader("Accept-Encoding", "identity")
            .build();
    private final JsonMapper mapper = JsonMapper.builder().build();

    // Returns the walking path as a list of points, or an empty list on failure
public List<Areas> getPath(double stLat, double stLng, double endLat, double endLng) {
    List<Areas> path = new ArrayList<>();
    try {
        String url = String.format(Locale.US,
                "https://routing.openstreetmap.de/routed-foot/route/v1/driving/%f,%f;%f,%f"
                        + "?overview=full&geometries=geojson",
                stLng, stLat, endLng, endLat);

        // read the response as plain text first so we can see what came back
        String body = restClient.get().uri(URI.create(url)).retrieve().body(String.class);
        if (body == null || !body.trim().startsWith("{")) {
            System.err.println("Routing returned non-JSON: "
                    + (body == null ? "null" : body.substring(0, Math.min(200, body.length()))));
            return path;
        }

        JsonNode root = mapper.readTree(body);
        for (JsonNode c : root.path("routes").path(0).path("geometry").path("coordinates")) {
            // GeoJSON order is [lng, lat]
            path.add(new Areas(c.path(1).asDouble(), c.path(0).asDouble()));
        }
    } catch (Exception e) {
        System.err.println("Routing failed: " + e + " / cause: " + e.getCause());
    }
    return path;
}
}