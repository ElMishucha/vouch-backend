//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.michaelb.vouch.integration.openai.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.michaelb.vouch.integration.openai.OpenAIClient;
import com.michaelb.vouch.model.response.ChatGPTWebSearchResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class ChatGPTWebSearchService {
    private final OpenAIClient openAIClient;
    private final Gson gson = new Gson();

    public ChatGPTWebSearchService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    public ChatGPTWebSearchResponse webSearch(String claimSummary) {
        Map<String, Object> payload = getPayloadMap(claimSummary);
        String response = this.openAIClient.sendRequest(payload);
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        if (jsonObject.has("error")) {
            String errorMsg = jsonObject.get("error").getAsString();
            System.err.println("Error from ChatGPT: " + errorMsg);
            return null;
        } else if (jsonObject.has("sources")) {
            System.out.println(response);
            return this.gson.fromJson(response, ChatGPTWebSearchResponse.class);
        } else {
            System.err.println("Unexpected response format.");
            return null;
        }
    }

    @NotNull
    private static Map<String, Object> getPayloadMap(String claimSummary) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4o-mini");

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a fact-checking assistant. Given a concise and neutral summary of a user claim, your task is to simulate a web search and return a list of relevant, trustworthy sources that discuss or relate to the topic.\n\n### Requirements:\n- Do NOT analyze or fact-check the claim.\n- Only simulate a realistic, diverse set of sources one would find through a web search.\n- Focus on credible news sites, encyclopedias, government reports, or scientific articles.\n- Provide a short snippet (1–2 sentences) summarizing what each source says about the topic.\n- If the source clearly supports or refutes the claim, hint at it in the snippet, but do NOT judge it directly.\n- Include publication date in YYYY-MM-DD format when available.\n- Return strictly in JSON format, no extra text.\n\n### Output Format:\n{\n  \"sources\": [\n    {\n      \"title\": \"Title of the article\",\n      \"url\": \"https://example.com/article\",\n      \"snippet\": \"A short 1–2 sentence description of what the article says about the claim.\",\n      \"source\": \"BBC\", \n      \"date\": \"2024-11-01\"\n    }\n  ]\n}\n\n### Rules:\n- Always return valid JSON.\n- If no relevant sources can be found, return:\n  {\n    \"error\": \"No relevant sources found for the given summary.\"\n  }\n");
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", claimSummary);
        messages.add(userMessage);

        payload.put("messages", messages);
        return payload;
    }
}
