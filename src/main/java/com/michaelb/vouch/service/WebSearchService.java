//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.michaelb.vouch.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.michaelb.vouch.util.OpenAIClient;
import com.michaelb.vouch.model.Source;
import com.michaelb.vouch.model.response.WebSearchResponse;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class WebSearchService {
    private final OpenAIClient openAIClient;
    private static final Gson gson = new Gson();

    public WebSearchService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    public WebSearchResponse webSearch(String claimSummary) {
        String payload = getPayload(claimSummary);
        String text = this.openAIClient.sendRequest(payload);

        System.out.println(text);

        JsonObject jsonObject = JsonParser.parseString(text).getAsJsonObject();

        List<Source> sources = extractSources(text);
        String error = jsonObject.has("error") && !jsonObject.get("error").isJsonNull()
                ? jsonObject.get("error").getAsString()
                : null;

        return new WebSearchResponse(sources, error);
    }

    List<Source> extractSources(String jsonText) {
        JsonObject payload = JsonParser.parseString(jsonText).getAsJsonObject();
        JsonArray sourcesArray = payload.get("sources").getAsJsonArray();

        return new Gson().fromJson(sourcesArray, new TypeToken<List<Source>>() {
        }.getType());
    }

    private static String getPayload(String claimSummary) {
        String instructions = """
                You are Stage2_WebSearch.
                
                TASK \s
                - Using the claim provided, list up to five recent (≤ 12 months old) credible sources. \s
                - Acceptable domains: major international news outlets (AP, Reuters, BBC, NYT, etc.), established fact-checkers (PolitiFact, Snopes, Full Fact, FactCheck.org), or academic/government sites ending in .edu or .gov. \s
                - Exclude blogs, social media, pay-for-placement sites, heavily biased publishers, and content farms. \s
                
                OUTPUT SCHEMA \s
                { "sources": [ { "title": ..., "source": ..., "date": ..., "url": ... } ] } \s
                If no qualifying sources exist, return { "error": "No credible recent sources found for this claim" }.
                
                STRICT FORMATTING RULES \s
                - Output only the JSON object—nothing before or after it. \s
                - All field values must be plain UTF-8 English with normal spacing. \s
                - No Markdown, no asterisks, no underscores, no code fences, no smart quotes, no ellipses, no extra punctuation.
                """;

        return """
                {
                  "model": "gpt-4o-mini",
                  "tools": [
                    {
                      "type": "web_search_preview",
                      "search_context_size": "medium"
                    }
                  ],
                  "instructions": %s,
                  "input": %s,
                  "text": {
                    "format": {
                      "type": "json_schema",
                      "name": "source_extraction",
                      "schema": {
                        "type": "object",
                        "properties": {
                          "sources": {
                            "type": "array",
                            "items": {
                              "type": "object",
                              "properties": {
                                "title":   { "type": "string" },
                                "url":     { "type": "string" },
                                "snippet": { "type": "string" },
                                "source":  { "type": "string" },
                                "date":    { "type": "string" }
                              },
                              "required": ["title","url","snippet","source","date"],
                              "additionalProperties": false
                            }
                          },
                          "error": {
                            "type": ["string", "null"]
                          }
                        },
                        "required": ["sources", "error"],
                        "additionalProperties": false
                      },
                      "strict": true
                    }
                  }
                }
                """.formatted(gson.toJson(instructions), gson.toJson(claimSummary));
    }
}
