package com.example.demo;

//the record classifier only takes in raw data, not expected to be changed much
public record AiResult(Status hasSide, Status hasBike, Status hasTree, Status hasGreen) {}
