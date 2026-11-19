package com.example.demo;

import java.util.List;

//DONE!!
public record Roads(double lat, double lng, Status hasSide, Status hasBike, Status hasTree, Status hasGreen,
        Highway streetType, AiResult ai, List<Improvements> improvements) {
    public Roads(double lat, double lng, Status hasSide, Status hasBike, Status hasTree, Status hasGreen,
            Highway streetType, AiResult ai) {
        this(lat, lng, hasSide, hasBike, hasTree, hasGreen, streetType, ai, List.of());
    }
}
