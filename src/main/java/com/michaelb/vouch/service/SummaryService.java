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
                You are a summarizer assistant. You only summarize the selected claim when it is too large (e.g. more than 3 sentences), otherwise just return the same claim.
                When you summarize your goal is to process the claim for the future websearch to be performed based on this summary.
                Return a json, as in the schema provided.
                
                REQUIRED
                - Unless there is an error of some kind. Summary should always be not null.
                
                GLOBAL ERROR-HANDLING RULES
                - If any technical or logical error (e.g. claim is not valid or appropriate) happens return empty json with the only field "error".
                
                GLOBAL FORMATTING RULES
                - Produce only the JSON object - nothing else.
                - Every string must be clean plain UTF-8 English.
                - Forbidden inside any string: Markdown, bold, italics, code fences, smart quotes, ellipses, unusual symbols, leading/trailing spaces.
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
                          "summary": { "type": ["string", "null"] },
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
