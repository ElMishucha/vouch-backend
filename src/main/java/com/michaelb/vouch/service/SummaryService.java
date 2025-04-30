//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.michaelb.vouch.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.michaelb.vouch.util.OpenAIClient;
import com.michaelb.vouch.model.response.SummaryResponse;

import org.springframework.stereotype.Service;

@Service
public class SummaryService {
    private final OpenAIClient openAIClient;
    private static final Gson gson = new Gson();

    public SummaryService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    public SummaryResponse summarizeClaimForSearch(String claim) {
        String payload = getPayload(claim);
        String text = this.openAIClient.sendRequest(payload);

        System.out.println(text);

        JsonObject jsonObject = JsonParser.parseString(text).getAsJsonObject();

        String summary = jsonObject.get("summary").getAsString();
        String error = jsonObject.has("error") && !jsonObject.get("error").isJsonNull()
                ? jsonObject.get("error").getAsString()
                : null;

        return new SummaryResponse(summary, error);
    }

    private static String getPayload(String claim) {
        String instructions = """
                You are Stage1_Summarizer.
                
                TASK \s
                - Receive one user-highlighted passage. \s
                - Output exactly one JSON object that the calling code will validate against its schema. \s
                - Inside the single required string field, write ONE neutral sentence (≤ 20 words) that captures the central claim. \s
                - Do not add opinion, context, hedging, or extra sentences. \s
                - If the input is empty, meaningless, or contains no clear factual claim, output the error object: { "error": "No valid claim provided" }.
                
                STRICT FORMATTING RULES \s
                - Never output anything outside the JSON object. \s
                - All string content must be plain UTF-8 English. \s
                - Absolutely no Markdown, bold, italics, code blocks, quote characters, smart quotes, ellipses, emojis, or other symbols.
                """;

        return """
                {
                  "model": "gpt-4.1-nano",
                  "instructions": %s,
                  "input": %s,
                  "text": {
                    "format": {
                      "type": "json_schema",
                      "name": "summary_response",
                      "schema": {
                        "type": "object",
                        "properties": {
                          "summary": { "type": "string" },
                          "error": { "type": ["string", "null"] }
                        },
                        "required": ["summary", "error"],
                        "additionalProperties": false
                      },
                      "strict": true
                    }
                  }
                }
                """.formatted(gson.toJson(instructions), gson.toJson(claim));
    }
}
