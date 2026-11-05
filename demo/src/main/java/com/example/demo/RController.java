package com.example.demo;

import java.util.List;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/* General Idea:

	Data integration portion of City Improvement project
	Uses SpringBoot and Java and maven i guess for backend development

	Tasks:
	- Get information from the web server
		- Take in the OpenStreetMaps imaging data
		- Reduce feedable information for optimization:
			- Only check sidewalk side facing images
			- Pictures per 500m
			- Skip motorways
	- Post information to AI agent
		- Feed imaging to AI for analysis
	- Get output from AI
		- Take in results from AI
		- Parse data into objects

*/ 
@SpringBootApplication
@RestController
public class RController {

    private final RService routeService;

	//requests data from frontend
    public RController(RService routeService) {
        this.routeService = routeService;
    }

	//returns data retrieved into RRequest record
    @PostMapping("/analyze-route")
    public List<Roads> analyze(@RequestBody RRequest request) {
        return routeService.analyze(request);
    }
}
