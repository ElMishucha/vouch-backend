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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class ChatGPTSummaryService {
    private final OpenAIClient openAIClient;
    private final Gson gson = new Gson();

    public ChatGPTSummaryService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    public ChatGPTSummaryResponse summarizeClaimForSearch(String claim) {
        Map<String, Object> payload = getPayloadMap(claim);
        String response = this.openAIClient.sendRequest(payload);
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        if (jsonObject.has("error")) {
            String errorMsg = jsonObject.get("error").getAsString();
            System.err.println("Error from ChatGPT: " + errorMsg);
            return null;
        } else if (jsonObject.has("summary")) {
            System.out.println(response);
            return new ChatGPTSummaryResponse(jsonObject.get("summary").getAsString());
        } else {
            System.err.println("Unexpected response format.");
            return null;
        }
    }

    @NotNull
    private static Map<String, Object> getPayloadMap(String claim) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4o-mini");

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant that processes user-submitted claims for a fact-checking system. Your task is to neutrally and briefly summarize the user's selected text so it can be used for a web search query.\n\n### Requirements:\n- Do NOT judge the truthfulness of the claim.\n- Focus on extracting the core factual idea behind the text.\n- Keep the summary objective, concise, and clear.\n- Strip out emotional or persuasive language.\n- Be mindful of potential ambiguity—disambiguate if possible based on context.\n- Output the result strictly in the following JSON format, with no explanation or extra text.\n\n### JSON Format:\n{\n  \"summary\": \"<clean and neutral summary for web search>\"\n}\n\n### Rules:\n- Your response must be valid JSON only.\n- Do not include any commentary or additional output.\n- If the input is empty or purely nonsensical, return this instead:\n  {\n    \"error\": \"Invalid claim: unable to summarize.\"\n  }\n");
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", claim);
        messages.add(userMessage);

        payload.put("messages", messages);
        return payload;
    }
}
