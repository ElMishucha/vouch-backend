//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.michaelb.vouch.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.michaelb.vouch.util.OpenAIClient;
import com.michaelb.vouch.model.Source;
import com.michaelb.vouch.model.aspect.*;
import com.michaelb.vouch.model.response.AnalysisResponse;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class AnalysisService {
    private final OpenAIClient openAIClient;
    private static final Gson gson = new Gson();

    public AnalysisService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    public AnalysisResponse analyze(String claim, List<Source> sources) {
        String payload = getPayload(claim, sources);
        String text = this.openAIClient.sendRequest(payload);

        System.out.println(text);

        JsonObject jsonObject = JsonParser.parseString(text).getAsJsonObject();

        FinalVerdictAspect finalVerdict = !jsonObject.get("finalVerdict").isJsonNull()
                ? gson.fromJson(jsonObject.getAsJsonObject("finalVerdict"), FinalVerdictAspect.class)
                : null;

        FactualClarificationAspect factualClarification = !jsonObject.get("factualClarification").isJsonNull()
                ? gson.fromJson(jsonObject.getAsJsonObject("factualClarification"), FactualClarificationAspect.class)
                : null;

        SourceSupportAspect sourceSupport = !jsonObject.get("sourceSupport").isJsonNull()
                ? gson.fromJson(jsonObject.getAsJsonObject("sourceSupport"), SourceSupportAspect.class)
                : null;

        PropagandaAspect propaganda = !jsonObject.get("propaganda").isJsonNull()
                ? gson.fromJson(jsonObject.getAsJsonObject("propaganda"), PropagandaAspect.class)
                : null;

        PersuasiveStrategiesAspect persuasiveStrategies = !jsonObject.get("persuasiveStrategies").isJsonNull()
                ? gson.fromJson(jsonObject.getAsJsonObject("persuasiveStrategies"), PersuasiveStrategiesAspect.class)
                : null;

        String error = jsonObject.has("error") && !jsonObject.get("error").isJsonNull()
                ? jsonObject.get("error").getAsString()
                : null;

        return new AnalysisResponse(finalVerdict, factualClarification, sourceSupport, propaganda, persuasiveStrategies, error);
    }

    private static String getPayload(String claimSummary, List<Source> sources) {
        String instructions = """
                You are Stage3_Analyzer.
                
                INPUTS \s
                - A claim string. \s
                - An array of source objects (title, source, date, url).
                
                TASK \s
                Fill the provided fact_check_aspects JSON schema accurately.
                
                FIELD-SPECIFIC RULES \s
                - sourceSupport.sources[*]: \s
                  - For each source, set "stance" to Support / Deny / Neutral. \s
                  - Add a very short reasoning phrase (plain text). \s
                - factualClarification: \s
                  - Summarize what is known and unknown, citing publisher names when helpful (“Reuters states…”, “CDC reports…”). \s
                  - Do NOT mention “Source 1”, “Source 2”, or numbering of any kind. \s
                - authenticityScore: \s
                  - A number from 0 to 100 representing how factually accurate and reliable the claim is based on available evidence.
                  - 0 = Completely false, fabricated, or unsupported.
                  - 100 = Fully verified, trustworthy, and supported by strong evidence.
                  - This score should be derived from both the Source Support stances and the Factual Clarification content, using weighted logic:
                    - Strongly Deny - 0%
                    - Deny - 25%
                    - Neutral - 50%
                    - Support - 75%
                    - Strongly Support - 100%
                  - Factual Clarification should influence the final score by analyzing the quality and relevance of the evidence (e.g., presence of primary sources, clarity of proof, or gaps in verification).
                  - The final score must reflect both the stance distribution and the depth of supporting evidence, not just agreement.
                - propagandaTechniques and persuasiveStrategies: \s
                  - Set to null unless the provided sources directly support content for these sections.
                
                GLOBAL FORMATTING RULES \s
                - Produce only the JSON object—nothing else. \s
                - Every string must be clean plain UTF-8 English. \s
                - Forbidden inside any string: Markdown, bold, italics, code fences, smart quotes, ellipses, unusual symbols, leading/trailing spaces.
                """;

        String claimText = "Claim: " + claimSummary + "\n\nSources: " + gson.toJson(sources);

        return """
                {
                  "model": "gpt-4o",
                  "instructions": %s,
                  "input": %s,
                  "text": {
                    "format": {
                        "type": "json_schema",
                        "name": "fact_check_aspects",
                        "strict": true,
                        "schema": {
                            "type": "object",
                            "properties": {
                              "thoughtProcess": { "type": "string" },
                              "sourceSupport": {
                                "type": "object",
                                "properties": {
                                  "thoughtProcess": { "type": "string" },
                                  "body": { "type": "string" },
                                  "sources": {
                                    "type": "array",
                                    "items": {
                                      "type": "object",
                                      "properties": {
                                        "title": { "type": "string" },
                                        "url": { "type": "string" },
                                        "snippet": { "type": "string" },
                                        "source": { "type": "string" },
                                        "date": { "type": "string" },
                                        "thoughtProcess": { "type": "string" },
                                        "body": { "type": "string" },
                                        "support": {
                                          "type": "string",
                                          "enum": ["Strongly Deny", "Deny", "Neutral", "Support", "Strongly Support"]
                                        }
                                      },
                                      "required": ["title", "url", "snippet", "source", "date", "thoughtProcess", "body", "support"],
                                      "additionalProperties": false
                                    }
                                  }
                                },
                                "required": ["thoughtProcess", "body", "sources"],
                                "additionalProperties": false
                              },
                              "propaganda": {
                                "type": ["object", "null"],
                                "properties": {
                                  "thoughtProcess": { "type": "string" },
                                  "body": { "type": "string" },
                                  "leftSideName": { "type": "string" },
                                  "rightSideName": { "type": "string" },
                                  "biasSlider": { "type": "integer" }
                                },
                                "required": ["thoughtProcess", "body", "leftSideName", "rightSideName", "biasSlider"],
                                "additionalProperties": false
                              },
                              "persuasiveStrategies": {
                                "type": ["object", "null"],
                                "properties": {
                                  "thoughtProcess": { "type": "string" },
                                  "body": { "type": "string" },
                                  "techniques": {
                                    "type": "array",
                                    "items": {
                                      "type": "object",
                                      "properties": {
                                        "name": { "type": "string" },
                                        "description": { "type": "string" }
                                      },
                                      "required": ["name", "description"],
                                      "additionalProperties": false
                                    }
                                  }
                                },
                                "required": ["thoughtProcess", "body", "techniques"],
                                "additionalProperties": false
                              },
                              "factualClarification": {
                                "type": ["object", "null"],
                                "properties": {
                                  "thoughtProcess": { "type": "string" },
                                  "body": { "type": "string" },
                                  "analysis": { "type": "string" }
                                },
                                "required": ["thoughtProcess", "body", "analysis"],
                                "additionalProperties": false
                              },
                              "finalVerdict": {
                                "type": ["object", "null"],
                                "properties": {
                                  "thoughtProcess": { "type": "string" },
                                  "body": { "type": "string" },
                                  "displayText": { "type": "string" },
                                  "authenticity": { "type": "integer" },
                                  "verdict": {
                                    "type": "string",
                                    "enum": ["True", "False", "Mixture", "Unproven"]
                                  }
                                },
                                "required": ["thoughtProcess", "body", "displayText", "authenticity", "verdict"],
                                "additionalProperties": false
                              },
                              "error": {
                                "type": ["string", "null"]
                              }
                            },
                            "required": ["thoughtProcess", "sourceSupport", "propaganda", "persuasiveStrategies", "factualClarification", "finalVerdict", "error"],
                            "additionalProperties": false
                        }
                    }
                  }
                }
                """.formatted(gson.toJson(instructions), gson.toJson(claimText));
    }

}
