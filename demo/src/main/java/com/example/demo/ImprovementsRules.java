package com.example.demo;

import java.util.ArrayList;
import java.util.List;

public class ImprovementsRules {

    private enum State {
        PRESENT, MISSING, UNKNOWN
    }

    // Present if either source says YES. Missing if either says NO (and none says
    // YES).
    private static State combine(Status osm, Status ai) {
        if (osm == Status.YES || ai == Status.YES)
        {
            return State.PRESENT;
        }
        
        if (osm == Status.NO || ai == Status.NO)
        {
            return State.MISSING;
        }

        return State.UNKNOWN;
    }

    private static Status aiValue(AiResult ai, java.util.function.Function<AiResult, Status> getter) {
        return ai == null ? Status.UNKNOWN : getter.apply(ai);
    }

    // Returns a new Roads with the Improvementss list filled in.
    // Rebuilds from the base fields, so it's safe to call on already-processed or
    // cached objects.
    public static Roads apply(Roads r) {
        List<Improvements> list = new ArrayList<>();

        // Sidewalks and bike lanes only make sense on ordinary streets
        boolean ordinaryStreet = r.streetType() == Highway.STREET;

        if (ordinaryStreet && combine(r.hasSide(), aiValue(r.ai(), AiResult::hasSide)) == State.MISSING) {
            list.add(Improvements.NEEDS_SIDEWALK);
        }
        if (ordinaryStreet && combine(r.hasBike(), aiValue(r.ai(), AiResult::hasBike)) == State.MISSING) {
            list.add(Improvements.NEEDS_BIKE_LANE);
        }
        // Trees: OSM never says NO (unmapped isn't "none"), so this relies on the AI
        // saying NO
        if (r.streetType() != Highway.MTRWAY && combine(r.hasTree(), aiValue(r.ai(), AiResult::hasTree)) == State.MISSING) {
            list.add(Improvements.NEEDS_TREES);
        }

        return new Roads(r.lat(), r.lng(), r.hasSide(), r.hasBike(), r.hasTree(),
                r.hasGreen(), r.streetType(), r.ai(), list);
    }
}