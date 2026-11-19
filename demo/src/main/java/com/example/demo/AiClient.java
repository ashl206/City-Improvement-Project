package com.example.demo;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class AiClient {

    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private static final String MODEL = "gemma3:4b";

    private static final String PROMPT = """
            You are an AI road accessibility image analyzer.
            Analyze the provided road photograph.
            Determine these four features:
            1. hasSide: Is a sidewalk visibly present?
            2. hasBike: Is a bicycle lane visibly present?
            3. hasTree: Are trees visibly present?
            4. hasGreen: Is green space, such as grass or landscaping, visibly present?

            Return ONLY valid JSON with exactly this structure:
            {"hasSide":"UNKNOWN","hasBike":"UNKNOWN","hasTree":"UNKNOWN","hasGreen":"UNKNOWN"}

            Rules:
            - Use only YES, NO, or UNKNOWN.
            - YES means the feature is visibly present.
            - NO means the image gives enough evidence that the feature is absent.
            - UNKNOWN means the image is unclear, obstructed, or does not show enough to decide.
            - Do not assume something is absent because it is outside the photograph.
            - Do not guess. No explanations or extra fields.
            """;

    private final JsonMapper mapper = JsonMapper.builder().build();
    private final RestClient restClient;

    public AiClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(300_000);   // local vision models can be slow
        this.restClient = RestClient.builder()
        .defaultHeader("Accept-Encoding", "identity")
        .requestFactory(factory)
        .build();
    }

    // Returns null if anything goes wrong, so one bad image never fails the route
    public AiResult analyze(byte[] image) {
        if (image == null) return null;
        try {
            String b64 = Base64.getEncoder().encodeToString(image);
            Map<String, Object> body = Map.of(
                    "model", MODEL,
                    "stream", false,
                    "format", "json",
                    "messages", List.of(Map.of(
                            "role", "user",
                            "content", PROMPT,
                            "images", List.of(b64))));

            JsonNode response = restClient.post().uri(OLLAMA_URL)
                    .body(body).retrieve().body(JsonNode.class);

            String content = response.path("message").path("content").asString("");
            JsonNode f = mapper.readTree(content);

            return new AiResult(
                    toStatus(f.path("hasSide").asString("")),
                    toStatus(f.path("hasBike").asString("")),
                    toStatus(f.path("hasTree").asString("")),
                    toStatus(f.path("hasGreen").asString("")));
        } catch (Exception e) {
            System.err.println("AI analysis failed: " + e.getMessage());
            return null;
        }
    }

    // Anything that isn't exactly YES or NO becomes UNKNOWN
    private Status toStatus(String v) {
        return switch (v.trim().toUpperCase()) {
            case "YES" -> Status.YES;
            case "NO" -> Status.NO;
            default -> Status.UNKNOWN;
        };
    }
}