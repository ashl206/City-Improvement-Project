package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class OSMController {
    
    //Send a location request to the app
    @RequestMapping("/hello")

    //Receive the app's response
    @ResponseBody
    public String helloGFG() {
        return "Hello GeeksForGeeks";
    }
}