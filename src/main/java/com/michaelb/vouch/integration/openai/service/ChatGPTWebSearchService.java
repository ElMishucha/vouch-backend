//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.michaelb.vouch.integration.openai.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.michaelb.vouch.integration.openai.OpenAIClient;
import com.michaelb.vouch.model.response.ChatGPTSummaryResponse;
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

        System.out.println(response);

        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        if (jsonObject.has("error")) {
            String errorMsg = jsonObject.get("error").getAsString();
            return new ChatGPTWebSearchResponse(null, errorMsg);
        } else if (jsonObject.has("sources")) {
            System.out.println(response);
            return this.gson.fromJson(response, ChatGPTWebSearchResponse.class);
        } else {
            return new ChatGPTWebSearchResponse(null, "Unexpected response format.");
        }
    }

    @NotNull
    private static Map<String, Object> getPayloadMap(String claimSummary) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4o-mini");
//        payload.put("model", "gpt-4.1");
//        payload.put("model", "gpt-4o-search-preview");

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", """
                You are a fact-checking assistant. Given a concise and neutral summary of a user claim, your task is to simulate a web search and return a list of relevant, trustworthy sources that discuss or relate to the topic.
                
                ### Requirements:
                - Do NOT analyze or fact-check the claim.
                - Only simulate a realistic, diverse set of sources one would find through a web search.
                - Focus on credible news sites, encyclopedias, government reports, or scientific articles.
                - Provide a short snippet (1–2 sentences) summarizing what each source says about the topic.
                - If the source clearly supports or refutes the claim, hint at it in the snippet, but do NOT judge it directly.
                - Include publication date in YYYY-MM-DD format when available.
                - Return strictly in JSON format, no extra text.
                
                ### Output Format:
                {
                  "sources": [
                    {
                      "title": "Title of the article",
                      "url": "https://example.com/article",
                      "snippet": "A short 1–2 sentence description of what the article says about the claim.",
                      "source": "BBC",\s
                      "date": "2024-11-01"
                    }
                  ]
                }
                
                ### Rules:
                - Always return valid JSON.
                - If no relevant sources can be found, return:
                {
                  "error": "No relevant sources found for the given summary."
                }
                """);
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", claimSummary);
        messages.add(userMessage);

        payload.put("messages", messages);

//        // Add tools
//        List<Map<String, String>> tools = new ArrayList<>();
//        Map<String, String> tool = new HashMap<>();
//        tool.put("type", "web_search_preview");
//        tool.put("search_context_size", "low");
//        tools.add(tool);
//        payload.put("tools", tools);
//
//        // Add tool choice
//        payload.put("tool_choice", "auto");

        return payload;
    }
}
