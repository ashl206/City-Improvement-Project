package com.example.demo;

//the record classifier only takes in raw data, not expected to be changed much
public record AIInput(Status AISide, Status AIBike, Status AITree, Status AIGreen, String AINotes){}
