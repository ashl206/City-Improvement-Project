package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
//Basic file is completed, just needs the Ai info
import org.springframework.web.server.ResponseStatusException;

@Service
public class RService {
    private static final double INTERVAL_METERS = 500;
    private static final int MAX_POINTS = 20;
    private final OsmClient osmClient;

    public RService(OsmClient osmClient) {
        this.osmClient = osmClient;
    }

    public List<Roads> analyze(RRequest request) {
        // Sample per 500m
        List<Areas> points = RouteSampler.sample(request.stLat(), request.stLng(), request.endLat(), request.endLng(),
                INTERVAL_METERS);

        // ensuring route isn't too long
        if (points.size() > MAX_POINTS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route too long. Max " + MAX_POINTS + " points.");
        }

        // Fake data for now. Later this calls OsmClient, ImageClient, AiClient.
        List<Roads> result = new ArrayList<>();

        for (int i = 0; i < points.size(); i++) {
            Areas p = points.get(i);
            result.add(osmClient.getOsmData(p.lat(), p.lng()));
            if (i < points.size() - 1) { // no need to wait after the last point
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return result;
    }
}
