package com.example.demo;

import tools.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OsmClient {

    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";
    private final RestClient restClient = RestClient.create();

    private Roads fetch(double lat, double lng) {
        try {
            JsonNode root = queryWithRetry(lat, lng);
            return parse(lat, lng, root);
        } catch (Exception e) {
            // If Overpass fails, don't crash the whole route
            System.err.println("OSM lookup failed at " + lat + "," + lng + ": " + e.getMessage());
            return new Roads(lat, lng, Status.UNKNOWN, Status.UNKNOWN, Status.UNKNOWN,
                    Status.UNKNOWN, Highway.UNKNOWN, null);
        }
    }

    private final Map<String, Roads> cache = new ConcurrentHashMap<>();

    public Roads getOsmData(double lat, double lng) {
        String key = String.format(Locale.US, "%.5f,%.5f", lat, lng);
        return cache.computeIfAbsent(key, k -> fetch(lat, lng));
    }

    private JsonNode queryWithRetry(double lat, double lng) throws InterruptedException {
        long waitMs = 1000;
        for (int attempt = 1; attempt <= 4; attempt++) {
            try {
                return query(lat, lng);
            } catch (Exception e) {
                if (attempt == 4)
                    throw e;
                System.err.println("Overpass busy, retrying in " + waitMs + " ms");
                Thread.sleep(waitMs);
                waitMs *= 2; // 1s, 2s, 4s
            }
        }
        return null;
    }

    private JsonNode query(double lat, double lng) {
        // Locale.US matters: some locales print decimals with commas, which breaks the
        // query
        String q = String.format(Locale.US, """
                [out:json][timeout:25];
                (
                  way(around:25,%1$f,%2$f)["highway"];
                  node(around:30,%1$f,%2$f)["natural"="tree"];
                  way(around:50,%1$f,%2$f)["leisure"="park"];
                  way(around:50,%1$f,%2$f)["landuse"="grass"];
                );
                out tags;
                """, lat, lng);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("data", q);

        return restClient.post()
                .uri(OVERPASS_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);
    }

    private Roads parse(double lat, double lng, JsonNode root) {
        JsonNode roadTags = null; // best road found
        JsonNode anyWayTags = null; // fallback (footway, cycleway...)
        boolean tree = false;
        boolean green = false;

        for (JsonNode el : root.path("elements")) {
            JsonNode tags = el.path("tags");
            if (tags.has("highway")) {
                Highway type = mapHighway(tags.path("highway").asString());
                if (anyWayTags == null)
                    anyWayTags = tags;
                if (roadTags == null && (type == Highway.STREET || type == Highway.MTRWAY)) {
                    roadTags = tags;
                }
            }
            if ("tree".equals(tags.path("natural").asString()))
                tree = true;
            if ("park".equals(tags.path("leisure").asString())
                    || "grass".equals(tags.path("landuse").asString()))
                green = true;
        }

        JsonNode chosen = roadTags != null ? roadTags : anyWayTags;
        if (chosen == null) {
            return new Roads(lat, lng, Status.UNKNOWN, Status.UNKNOWN,
                    tree ? Status.YES : Status.UNKNOWN,
                    green ? Status.YES : Status.UNKNOWN,
                    Highway.UNKNOWN, null);
        }

        return new Roads(lat, lng,
                mapSidewalk(chosen),
                mapBike(chosen),
                tree ? Status.YES : Status.UNKNOWN, // no tree mapped != no trees
                green ? Status.YES : Status.UNKNOWN,
                mapHighway(chosen.path("highway").asString()),
                null);
    }

    private Highway mapHighway(String value) {
        return switch (value) {
            case "footway", "path", "pedestrian", "steps" -> Highway.PED;
            case "motorway", "motorway_link", "trunk", "trunk_link" -> Highway.MTRWAY;
            case "residential", "living_street", "unclassified", "service",
                    "tertiary", "secondary", "primary" ->
                Highway.STREET;
            default -> Highway.UNKNOWN;
        };
    }

    private Status mapSidewalk(JsonNode tags) {
        String[] keys = { "sidewalk", "sidewalk:both", "sidewalk:left", "sidewalk:right" };
        for (String key : keys) {
            String v = tags.path(key).asString("");
            if (v.equals("both") || v.equals("left") || v.equals("right") || v.equals("yes")) {
                return Status.YES;
            }
            if (v.equals("no") || v.equals("none"))
                return Status.NO;
        }
        return Status.UNKNOWN; // includes sidewalk=separate and missing tags
    }

    private Status mapBike(JsonNode tags) {
        String[] keys = { "cycleway", "cycleway:both", "cycleway:left", "cycleway:right" };
        for (String key : keys) {
            String v = tags.path(key).asString("");
            if (v.equals("lane") || v.equals("track") || v.equals("shared_lane")) {
                return Status.YES;
            }
            if (v.equals("no"))
                return Status.NO;
        }
        return Status.UNKNOWN;
    }
}