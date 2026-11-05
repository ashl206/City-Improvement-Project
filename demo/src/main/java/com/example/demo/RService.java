package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
//Basic file is completed, just needs the Ai info
@Service 
public class RService {
    
    public List<Roads> analyze(RRequest request) {
        // Fake data for now. Later this calls OsmClient, ImageClient, AiClient.
        List<Roads> result = new ArrayList<>();
        result.add(new Roads(request.stLat(), request.stLng(),
                Status.YES, Status.NO, Status.UNKNOWN, Status.NO, Highway.STREET));
        result.add(new Roads(request.endLat(), request.endLng(),
                Status.NO, Status.NO, Status.NO, Status.YES, Highway.STREET));
        return result;
    }
}
