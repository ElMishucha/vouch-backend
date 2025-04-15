//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.michaelb.vouch.integration.openai.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.michaelb.vouch.integration.openai.OpenAIClient;
import com.michaelb.vouch.model.Source;
import com.michaelb.vouch.model.response.ChatGPTAnalysisResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.michaelb.vouch.model.response.ChatGPTWebSearchResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class ChatGPTAnalysisService {
    private final OpenAIClient openAIClient;
    private final Gson gson = new Gson();

    public ChatGPTAnalysisService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    public ChatGPTAnalysisResponse analyze(String claim, List<Source> sources) {
        Map<String, Object> payload = getPayloadMap(claim, sources);

        String response = this.openAIClient.sendRequest(payload);
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

        if (jsonObject.has("error")) {
            String errorMsg = jsonObject.get("error").getAsString();
            return new ChatGPTAnalysisResponse(errorMsg);
        } else if (jsonObject.has("finalVerdict")) {
            System.out.println(response);
            return this.gson.fromJson(jsonObject, ChatGPTAnalysisResponse.class);
        } else {
            return new ChatGPTAnalysisResponse("Unexpected response format.");
        }
    }

    @NotNull
    private static Map<String, Object> getPayloadMap(String claim, List<Source> sources) {
        Gson gson = new Gson();
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4o-mini");
//        payload.put("model", "gpt-4.1");

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", """
                You are a fact-checking assistant. You will be given:
                  - A user-submitted claim
                  - A list of web sources (already fetched and provided to you)
                
                  Your job is to analyze the sources and generate a structured JSON response with several Aspects of the fact-check. You must not search the web or assume any information that is not explicitly present in the sources provided.
                
                  ### Your Tasks:
                  1. Carefully read the claim.
                  2. Review all provided sources to determine:
                     - What is factually supported or not
                     - What reliable information is available
                     - Whether there is bias or persuasive language
                  3. Based on the sources, generate only the Aspects that are relevant for this claim. If an Aspect is not applicable, you must set it to `null`. Do not generate empty shells or filler content.
                
                  ### Aspects Format (Output JSON Schema):
                
                  {
                    "thoughtProcess": "Internal reasoning about the sources and how they inform each aspect. Do not include extraneous instructions or developer notes.",
                
                    "sourceSupport": {
                      "thoughtProcess": "Summarize how the sources individually treat the claim — do they support it, deny it, or offer mixed signals? Think through how to assign the right labels and short explanations.",
                      "body": "Overview of which sources support or deny the claim.",
                      "sources": [
                        {
                          "title": "Source Title",
                          "url": "https://example.com",
                          "date": "yyyy-mm-dd",
                          "thoughtProcess": "A brief explanation of how you interpreted the source in relation to the claim, and how you decided its support level.",
                          "body": "Brief explanation of what this source says about the claim.",
                          "support": "Strongly Deny | Deny | Neutral | Support | Strongly Support"
                        }
                      ]
                    },
                
                    "propaganda": {
                      "thoughtProcess": "Consider whether the claim uses emotionally charged or biased language. Identify possible perspectives (leftSide/rightSide) and decide if there’s any favoritism in tone.",
                      "body": "If present, explain the use of biased or emotionally manipulative language.",
                      "leftSideName": "",
                      "rightSideName": "",
                      "biasSlider": 0-100
                    },
                
                    "persuasiveStrategies": {
                      "thoughtProcess": "Identify persuasive techniques in the claim, such as fear, emotional appeal, or exaggeration. Explain why they’re used and how they affect the reader.",
                      "body": "Explain how the claim uses techniques to influence the reader.",
                      "techniques": [
                        {
                          "name": "Loaded Language | Fear | Emotional Appeal | Bandwagon | ...",
                          "description": "What it means and how it was used in the claim."
                        }
                      ]
                    },
                
                    "factualClarification": {
                      "thoughtProcess": "Explain how the claim compares to the facts found in the sources. Note what’s accurate, what’s misleading, and how you’ll clarify it in the body and analysis.",
                      "body": "Explanation of what is actually true based on the evidence.",
                      "analysis": "Detailed breakdown with citations or reasoning from sources."
                    },
                
                    "finalVerdict": {
                      "thoughtProcess": "Reflect on the overall weight of the evidence. Does it strongly support, refute, or complicate the claim? Justify your chosen verdict and authenticity score.",
                      "body": "A short summary of how confidently this claim is supported or refuted based on sources.",
                      "displayText": "A short sentence (3–12 words) summarizing the verdict for display in the UI",
                      "authenticity": 0-100,
                      "verdict": "True | False | Mixture | Unproven"
                    }
                  }
                
                  ### Rules:
                  - Only use the provided sources. Do not guess or generate information outside them.
                  - Do not include any commentary or explanation outside the JSON.
                  - The response must be valid JSON.
                  - If an Aspect (like 'propaganda' or 'persuasiveStrategies') is not clearly supported by the content, set it to `null`. Do not include it with empty values or generic filler text.
                  - If something goes wrong, return this JSON error instead:
                  {
                    "error": "Failed to analyze claim with provided sources."
                  }
                """);
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "Claim: " + claim + "\n\nSources: " + gson.toJson(sources));
        messages.add(userMessage);

        payload.put("messages", messages);
        return payload;
    }
}
