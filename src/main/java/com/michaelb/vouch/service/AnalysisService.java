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
                You are the analysis assistant that is part of a fact-checking app.
                You are given a claim - a selected text from the website that needs to be fact-checked; and a list of sources that were found using WebSearch, that are related to the claim in some way.
                Your goal is to analyze and compare sources that are related to the claim and give the result in a json format, following the json schema provided, containing multiple Aspects.
                
                It is important to notice that each aspect and almost each json object has a thoughtProcess field.
                It is not going to be seen by user, however it is required that you fill them in for higher-quality results.
                
                It is also important that each "body" is a short and simple explanation of that aspect.
                Thought Process is required and it should be fairly concise.
                Your responses should be based on the thought process.
                
                It is also rea  lly important, that when estimating overall authenticity of the claim, you give the mix of estimate between two things:
                - Source Support - support from other websites on average.
                - Logic - pure logic from factual clarification.
                
                KEEP YOUR ANSWERS CONSISTENT. DO NOT CHANGE YOUR MIND FOR THE SAME CLAIM.
                
                The most important: CONSIDER THE CLAIM AS SOMEONE'S OPINION, NOT A FACT. DURING THOUGHT PROCESS AND ESTIMATING THE FINAL VERDICT ANSWER TO IT AS SOMEONE'S OPINION. THAT IS THE WHOLE POINT OF THIS APP. ANSWER TO IT AS SOMEONE'S OPINION.
                
                Aspects, their content, descriptions, and format in which you are supposed to return your response:
                - FinalVerdict
                  - thoughtProcess
                  - body - Explanation of your verdict
                  - displayText
                  - authenticity - Estimate of how much user can trust this claim. A number from 0 to 100, where 0 - completely false and 100 - definitely trustworthy. Again, it should be a mix of both source support and logic from factual clarification.
                    - For each source consider this:
                      - Strongly Deny - 0%
                      - Deny - 25%
                      - Neutral - 50%
                      - Support - 75%
                      - Strongly Support - 100%
                    - For sources calculate the average of these.
                    - For factual clarification, calculate your estimate based on how logical the fact sounds, from 0% to 100%
                    - Mix these two, try to keep it consistent by calculating the average.
                  - verdict - Your verdict worded
                - FactualClarification
                  - thoughtProcess
                  - body - The Fact. Should explain what happened from the logical side
                  - analysis - Explain how you came to this conclusion.
                - SourceSupport
                  - sources - Array of sources you are already given with. You DO NOT change the sources, their urls, etc. You analyze and add your analysis on top. These will be as well displayed among other aspects.
                    - source
                      - title
                      - url
                      - snippet
                      - source
                      - date
                      - thoughtProcess
                      - body - Quick explanation of why this source support/denies/neutral etc.
                      - support - One of Strongly Deny / Deny / Neutral / Support / Strongly Support the claim
                - Propaganda
                  - Use this aspect only if clearly applicable.
                  - thoughtProcess
                  - body
                  - leftSideName
                  - rightSideName
                  - biasSlider - A number from 0 to 100, which shows which side this claim is more biased towards.
                - PersuasiveStrategies
                  - Use this aspect only if clearly applicable.
                  - thoughtProcess
                  - techniques - Array of persuasive techniques used in the selected claim OR, IMPORTANT - if the message behind this claim, e.g. from factual clarification, is using some persuasive techniques.
                    - name
                    - description
                
                
                REQUIRED
                - Every thoughtProcess is REQUIRED and has to be not null
                - Unless there is an error of some kind. Aspects FinalVerdict, FactualClarification, and SourceSupport are required and must always be not null.
                
                GLOBAL ERROR-HANDLING RULES
                - If any technical or logical error (e.g. claim is not valid or appropriate, or sources are invalid) happens return empty json with the only field "error".
                
                GLOBAL FORMATTING RULES
                - Produce only the JSON object - nothing else.
                - Every string must be clean plain UTF-8 English.
                - Forbidden inside any string: Markdown, bold, italics, code fences, smart quotes, ellipses, unusual symbols, leading/trailing spaces.
                """;

        String claimText = "Claim: " + claimSummary + "\n\nSources: " + gson.toJson(sources);

        return """
                {
                  "model": "gpt-4.1",
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
                              "thoughtProcess": { "type": ["string", "null"] },
                              "sourceSupport": {
                                "type": ["object", "null"],
                                "properties": {
                                  "thoughtProcess": { "type": "string"},
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
