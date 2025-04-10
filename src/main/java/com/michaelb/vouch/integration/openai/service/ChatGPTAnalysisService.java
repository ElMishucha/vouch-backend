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
            System.err.println("Error from ChatGPT: " + errorMsg);
            return null;
        } else if (jsonObject.has("finalVerdict")) {
            System.out.println(response);
            return this.gson.fromJson(jsonObject, ChatGPTAnalysisResponse.class);
        } else {
            System.err.println("Unexpected response format.");
            return null;
        }
    }

    @NotNull
    private static Map<String, Object> getPayloadMap(String claim, List<Source> sources) {
        Gson gson = new Gson();
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4o-mini");

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a fact-checking assistant. You will be given:\n- A user-submitted claim\n- A list of web sources (already fetched and provided to you)\n\nYour job is to analyze the sources and generate a structured JSON response with several Aspects of the fact-check. You must not search the web or assume any information that is not explicitly present in the sources provided.\n\n### Your Tasks:\n1. Carefully read the claim.\n2. Review all provided sources to determine:\n    - What is factually supported or not\n    - What reliable information is available\n    - Whether there is bias or persuasive language\n3. Based on the sources, generate only the Aspects that are relevant for this claim. If an Aspect is not applicable, you may omit it entirely from the JSON.\n\n### Aspects Format (Output JSON Schema):\n\n{\n  // General reasoning across all aspects. This is GPT's internal logic.\n  // Use it to explain how you interpreted the sources and how you approached deciding which aspects to include.\n  // Do NOT reference the JSON structure or developer instructions here.\n  \"thoughtProcess\": \"Internal reasoning about the sources and how they inform each aspect. Do not include extraneous instructions or developer notes.\",\n\n  // --- SOURCE SUPPORT ---\n  // ✅ Use when: Multiple sources address the claim and show support, denial, or nuance.\n  // ❌ Skip when: There are no relevant sources or all sources are irrelevant to the main claim.\n  // Goal: Summarize what each individual source says and how much it supports or refutes the claim.\n  \"sourceSupport\": {\n    \"thoughtProcess\": \"Summarize how the sources individually treat the claim — do they support it, deny it, or offer mixed signals? Think through how to assign the right labels and short explanations.\",\n    \"body\": \"Overview of which sources support or deny the claim.\",\n    \"sources\": [\n      {\n        \"title\": \"Source Title\",\n        \"url\": \"https://example.com\",\n        \"date\": \"yyyy-mm-dd\"\n        \"thoughtProcess\": \"a brief explanation of how you interpreted the source in relation to the claim, and how you decided its support level.\",\n        \"body\": \"Brief explanation of what this source says about the claim.\"\n        \"support\": \"Strongly Deny | Deny | Neutral | Support | Strongly Support\",\n      }\n    ]\n  },\n\n  // --- PROPAGANDA / BIAS DETECTION ---\n  // ✅ Use when: The claim or its framing shows emotionally loaded or biased language.\n  // ❌ Skip when: The claim and sources are purely objective or neutral in tone.\n  // Goal: Identify framing, favoritism, or emotional manipulation. Define opposing perspectives clearly.\n  \"propaganda\": {\n    // leftSideName and rightSideName should reflect the opposing perspectives relevant to the claim (e.g., \"climate activists\" vs \"oil companies\"), not political ideologies.\n    \"thoughtProcess\": \"Consider whether the claim uses emotionally charged or biased language. Identify possible perspectives (leftSide/rightSide) and decide if there’s any favoritism in tone.\",\n    \"body\": \"If present, explain the use of biased or emotionally manipulative language.\",\n    \"leftSideName\": \"\",\n    \"rightSideName\": \"\",\n    \"biasSlider\": 0-100 // 0 = strongly favors leftSideName, 100 = strongly favors rightSideName, 50 = neutral\n  },\n\n  // --- PERSUASIVE STRATEGIES ---\n  // ✅ Use when: The claim uses rhetorical techniques to influence the reader (e.g. fear, exaggeration, bandwagon).\n  // ❌ Skip when: The claim is neutral or purely factual, without any noticeable strategy.\n  // Goal: Break down how the claim tries to sway the audience using persuasion, not just facts.\n  \"persuasiveStrategies\": {\n    \"thoughtProcess\": \"Identify persuasive techniques in the claim, such as fear, emotional appeal, or exaggeration. Explain why they’re used and how they affect the reader.\",\n    \"body\": \"Explain how the claim uses techniques to influence the reader.\",\n    \"techniques\": [\n      {\n        \"name\": \"Loaded Language | Fear | Emotional Appeal | Bandwagon | ...\",\n        \"description\": \"What it means and how it was used in the claim.\"\n      }\n    ]\n  },\n\n  // --- FACTUAL CLARIFICATION ---\n  // ✅ Use when: The claim is factual and can be clarified or corrected based on the provided sources.\n  // ❌ Skip when: The claim is purely opinion, satire, or not addressed by any sources.\n  // Goal: Correct any misinformation or add missing facts. Focus on **what is actually true** and **why**.\n  \"factualClarification\": {\n    \"thoughtProcess\": \"Explain how the claim compares to the facts found in the sources. Note what’s accurate, what’s misleading, and how you’ll clarify it in the body and analysis.\",\n    \"body\": \"Explanation of what is actually true based on the evidence.\",\n    \"analysis\": \"Detailed breakdown with citations or reasoning from sources.\"\n  },\n\n  // --- FINAL VERDICT ---\n  // ✅ Use for every claim. This is the summary judgment.\n  // Goal: Make a final decision on whether the claim is true, false, mixed, or unproven based on the **entirety of the provided sources**.\n  \"finalVerdict\": {\n    \"thoughtProcess\": \"Reflect on the overall weight of the evidence. Does it strongly support, refute, or complicate the claim? Justify your chosen verdict and authenticity score.\",\n    \"body\": \"A short summary of how confidently this claim is supported or refuted based on sources.\"\n    \"displayText\": \"A short sentence (3–12 words) summarizing the verdict for display in the UI\",\n    \"authenticity\": 0-100, // integer\n    \"verdict\": \"True | False | Mixture | Unproven\",\n  }\n}\n\n### Rules:\n- Only use the provided sources. Do not guess or generate information outside them.\n- Do not include any commentary or explanation outside the JSON.\n- The response must be valid JSON.\n- If something goes wrong, return this JSON error instead:\n{\n  \"error\": \"Failed to analyze claim with provided sources.\"\n}\n");
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "Claim: " + claim + "\n\nSources: " + gson.toJson(sources));
        messages.add(userMessage);

        payload.put("messages", messages);
        return payload;
    }
}
