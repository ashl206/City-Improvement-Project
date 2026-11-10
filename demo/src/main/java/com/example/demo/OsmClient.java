package com.example.demo;

import tools.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class OsmClient {

    private static final String[] SERVERS = {
            "https://overpass-api.de/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter",
            "https://overpass.private.coffee/api/interpreter"
    };
    private static final double ROAD_RADIUS = 25; // meters
    private static final double TREE_RADIUS = 30;
    private static final double GREEN_RADIUS = 50;

    private final RestClient restClient = RestClient.builder()
            .defaultHeader("User-Agent", "CityImprovementProject/0.1 (ashley206206@gmail.com)")
            .build();

    // a road or park: its tags plus its list of {lat, lng} points
    private record Way(JsonNode tags, List<double[]> pts) {
    }

    // ---------- public entry point: ONE request for the whole route ----------
    public List<Roads> getOsmData(List<Areas> points) {
        try {
            JsonNode root = queryWithRetry(points);
            return match(points, root);
        } catch (Exception e) {
            System.err.println("OSM lookup failed: " + e.getMessage());
            List<Roads> fallback = new ArrayList<>();
            for (Areas p : points) {
                fallback.add(new Roads(p.lat(), p.lng(), Status.UNKNOWN, Status.UNKNOWN,
                        Status.UNKNOWN, Status.UNKNOWN, Highway.UNKNOWN, null));
            }
            return fallback;
        }
    }

    // ---------- the request ----------
    private JsonNode queryWithRetry(List<Areas> points) throws Exception {

        StringBuilder coords = new StringBuilder();
        for (Areas p : points) {
            if (coords.length() > 0)
                coords.append(",");
            coords.append(String.format(Locale.US, "%.6f,%.6f", p.lat(), p.lng()));
        }
        String c = coords.toString();

        String q = "[out:json][timeout:25];("
                + "way(around:25," + c + ")[\"highway\"];"
                + "node(around:30," + c + ")[\"natural\"=\"tree\"];"
                + "way(around:50," + c + ")[\"leisure\"=\"park\"];"
                + "way(around:50," + c + ")[\"landuse\"=\"grass\"];"
                + ");out tags geom;";
        Exception last = null;
        long waitMs = 2000;
        for (int pass = 0; pass < 3; pass++) {
            for (String server : SERVERS) {
                try {
                    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
                    form.add("data", q);
                    return restClient.post()
                            .uri(server)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .body(form)
                            .retrieve()
                            .body(JsonNode.class);
                } catch (Exception ex) {
                    last = ex;
                    System.err.println("Failed on " + server + ": "
                            + ex.getClass().getSimpleName() + " - " + ex.getMessage());
                }
            }
            Thread.sleep(waitMs); // wait only after every server has been tried
            waitMs *= 2;
        }
        throw last;
    }

    // ---------- match results to each sampled point ----------
    private List<Roads> match(List<Areas> points, JsonNode root) {
        List<Way> roads = new ArrayList<>();
        List<Way> greens = new ArrayList<>();
        List<double[]> trees = new ArrayList<>();

        for (JsonNode el : root.path("elements")) {
            JsonNode tags = el.path("tags");
            if ("node".equals(el.path("type").asString())) {
                trees.add(new double[] { el.path("lat").asDouble(), el.path("lon").asDouble() });
                continue;
            }
            List<double[]> pts = new ArrayList<>();
            for (JsonNode g : el.path("geometry")) {
                pts.add(new double[] { g.path("lat").asDouble(), g.path("lon").asDouble() });
            }
            if (tags.has("highway"))
                roads.add(new Way(tags, pts));
            else if (tags.has("leisure") || tags.has("landuse"))
                greens.add(new Way(tags, pts));
        }

        List<Roads> result = new ArrayList<>();
        for (Areas p : points) {
            Way bestRoad = null, bestOther = null;
            double bestRoadDist = ROAD_RADIUS, bestOtherDist = ROAD_RADIUS;

            for (Way r : roads) {
                double d = distanceToWay(p, r);
                Highway type = mapHighway(r.tags().path("highway").asString());
                boolean realRoad = type == Highway.STREET || type == Highway.MTRWAY;
                if (realRoad && d < bestRoadDist) {
                    bestRoad = r;
                    bestRoadDist = d;
                }
                if (!realRoad && d < bestOtherDist) {
                    bestOther = r;
                    bestOtherDist = d;
                }
            }
            Way chosen = bestRoad != null ? bestRoad : bestOther;

            boolean tree = false;
            for (double[] t : trees) {
                if (distMeters(p.lat(), p.lng(), t[0], t[1]) <= TREE_RADIUS) {
                    tree = true;
                    break;
                }
            }
            boolean green = false;
            for (Way g : greens) {
                if (distanceToWay(p, g) <= GREEN_RADIUS) {
                    green = true;
                    break;
                }
            }

            Status treeStatus = tree ? Status.YES : Status.UNKNOWN; // unmapped != none
            Status greenStatus = green ? Status.YES : Status.UNKNOWN;

            if (chosen == null) {
                result.add(new Roads(p.lat(), p.lng(), Status.UNKNOWN, Status.UNKNOWN,
                        treeStatus, greenStatus, Highway.UNKNOWN, null));
            } else {
                result.add(new Roads(p.lat(), p.lng(),
                        mapSidewalk(chosen.tags()), mapBike(chosen.tags()),
                        treeStatus, greenStatus,
                        mapHighway(chosen.tags().path("highway").asString()), null));
            }
        }
        return result;
    }

    // ---------- geometry helpers ----------
    private static final double METERS_PER_DEGREE = 111320;

    private static double distMeters(double lat1, double lng1, double lat2, double lng2) {
        double dx = (lng2 - lng1) * Math.cos(Math.toRadians(lat1)) * METERS_PER_DEGREE;
        double dy = (lat2 - lat1) * METERS_PER_DEGREE;
        return Math.hypot(dx, dy);
    }

    // shortest distance from the point to any segment of the way
    private static double distanceToWay(Areas p, Way way) {
        List<double[]> pts = way.pts();
        if (pts.isEmpty())
            return Double.MAX_VALUE;
        if (pts.size() == 1)
            return distMeters(p.lat(), p.lng(), pts.get(0)[0], pts.get(0)[1]);
        double best = Double.MAX_VALUE;
        for (int i = 0; i < pts.size() - 1; i++) {
            best = Math.min(best, distToSegment(p, pts.get(i), pts.get(i + 1)));
        }
        return best;
    }

    // treat the point as the origin, flatten a and b to meters, find the closest
    // spot on a-b
    private static double distToSegment(Areas p, double[] a, double[] b) {
        double cos = Math.cos(Math.toRadians(p.lat()));
        double ax = (a[1] - p.lng()) * cos * METERS_PER_DEGREE;
        double ay = (a[0] - p.lat()) * METERS_PER_DEGREE;
        double bx = (b[1] - p.lng()) * cos * METERS_PER_DEGREE;
        double by = (b[0] - p.lat()) * METERS_PER_DEGREE;
        double dx = bx - ax, dy = by - ay;
        double lenSq = dx * dx + dy * dy;
        double t = lenSq == 0 ? 0 : Math.max(0, Math.min(1, -(ax * dx + ay * dy) / lenSq));
        return Math.hypot(ax + t * dx, ay + t * dy);
    }

    private Highway mapHighway(String value) {
        return switch (value) {
            case "footway", "path", "pedestrian", "steps" -> Highway.PED;
            case "motorway", "motorway_link" -> Highway.MTRWAY;
            case "trunk", "trunk_link", "residential", "living_street", "unclassified",
                    "service", "tertiary", "secondary", "primary" ->
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