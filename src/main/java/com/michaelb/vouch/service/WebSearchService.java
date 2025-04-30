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
                You are a websearch assistant that is part of a fact-checking app.
                
                Claim is the selected text from a website that is to be fact-checked.
                
                Your goal is to use your web-search tool to search for Sources that are related to this claim in some way.
                It can be strongly deny, deny, neutral, support, or strongly support.
                
                Find sources as credible as possible. Favor credible, trusted, and well-known sources.
                Find at least 3-5 DISTINCT sources.
                DO NOT REPEAT THE SAME SOURCES
                Find diverse sources for potential different point-of-views.
                
                FIND MOST RECENT NEWS RELATED TO IT.
                BETWEEN OLD NEWS AND RECENT NEWS ALWAYS FAVOR THE RECENT ONES.
                
                Return them in Json, as in the schema provided.
                
                You will return an array of sources.
                Each source contains:
                - title - Title of the article from that source
                - url - Url
                - snippet - Quick Snippet from that website that is related to the claim. 1-2 sentences MAX.
                - source - Name of the publisher.
                - date - Date when this source was published ("YYYY/MM/DD")
                
                REQUIRED
                - Unless there is an error of some kind. At least few Sources should be present.
                
                GLOBAL ERROR-HANDLING RULES
                - If any technical or logical error (e.g. no valid sources or invalid claim) happens return empty json with the only field "error".
                
                GLOBAL FORMATTING RULES
                - Produce only the JSON object-nothing else.
                - Every string must be clean plain UTF-8 English.
                - Forbidden inside any string: Markdown, bold, italics, code fences, smart quotes, ellipses, unusual symbols, leading/trailing spaces.
                """;

        return """
                {
                  "model": "gpt-4o",
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
