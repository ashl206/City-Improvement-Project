package com.example.demo;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class ImageClient {

    private static final double BOX_DEGREES = 0.0003;   // about 30 m each way

    private final RestClient restClient = RestClient.create();
    private final String token;

    public ImageClient(@Value("${mapillary.token:}") String token) {
        this.token = token;
    }

    // Returns the image bytes (JPEG), or null if there's no image or something fails
    public byte[] getImage(double lat, double lng) {
        if (token.isBlank()) {
            System.err.println("No Mapillary token set (MAPILLARY_TOKEN)");
            return null;
        }
        try {
            String bbox = String.format(Locale.US, "%f,%f,%f,%f",
                    lng - BOX_DEGREES, lat - BOX_DEGREES, lng + BOX_DEGREES, lat + BOX_DEGREES);
            // the token contains "|", which must be URL-encoded
            String url = "https://graph.mapillary.com/images"
                    + "?access_token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                    + "&fields=id,thumb_1024_url,computed_geometry"
                    + "&bbox=" + bbox + "&limit=10";

            JsonNode root = restClient.get().uri(URI.create(url)).retrieve().body(JsonNode.class);

            String bestUrl = null;
            double bestDist = Double.MAX_VALUE;
            for (JsonNode img : root.path("data")) {
                String thumb = img.path("thumb_1024_url").asString("");
                JsonNode c = img.path("computed_geometry").path("coordinates");
                if (thumb.isEmpty() || c.isMissingNode()) continue;
                double d = RouteSampler.distanceMeters(lat, lng, c.path(1).asDouble(), c.path(0).asDouble());
                if (d < bestDist) { bestDist = d; bestUrl = thumb; }
            }
            if (bestUrl == null) return null;   // no coverage here

            return restClient.get().uri(URI.create(bestUrl)).retrieve().body(byte[].class);
        } catch (Exception e) {
            System.err.println("Image lookup failed at " + lat + "," + lng + ": " + e.getMessage());
            return null;
        }
    }
}
