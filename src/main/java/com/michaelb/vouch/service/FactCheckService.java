//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.michaelb.vouch.service;

import com.google.gson.Gson;
import com.michaelb.vouch.integration.openai.service.ChatGPTAnalysisService;
import com.michaelb.vouch.integration.openai.service.ChatGPTSummaryService;
import com.michaelb.vouch.integration.openai.service.ChatGPTWebSearchService;
import com.michaelb.vouch.model.Source;
import com.michaelb.vouch.model.response.ChatGPTAnalysisResponse;
import com.michaelb.vouch.model.response.ChatGPTSummaryResponse;
import com.michaelb.vouch.model.response.ChatGPTWebSearchResponse;
import com.michaelb.vouch.model.response.FactCheckResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FactCheckService {
    private final ChatGPTSummaryService chatGPTSummaryService;
    private final ChatGPTWebSearchService chatGPTWebSearchService;
    private final ChatGPTAnalysisService chatGPTAnalysisService;
    private final Gson gson = new Gson();

    FactCheckService(ChatGPTSummaryService chatGPTSummaryService, ChatGPTWebSearchService chatGPTWebSearchService, ChatGPTAnalysisService chatGPTAnalysisService) {
        this.chatGPTSummaryService = chatGPTSummaryService;
        this.chatGPTWebSearchService = chatGPTWebSearchService;
        this.chatGPTAnalysisService = chatGPTAnalysisService;
    }

    public FactCheckResponse factCheck(String claim) {
        System.out.println("Fact-Checking claim: " + claim);

        ChatGPTSummaryResponse summaryResponse = this.chatGPTSummaryService.summarizeClaimForSearch(claim);
        if (summaryResponse.getError() != null)
            return new FactCheckResponse(null, summaryResponse.getError());
        String claimSummary = summaryResponse.getClaimSummary();

        ChatGPTWebSearchResponse webSearchResponse = this.chatGPTWebSearchService.webSearch(claimSummary);
        if (webSearchResponse.getError() != null)
            return new FactCheckResponse(null, webSearchResponse.getError());
        List<Source> sources = webSearchResponse.getSources();

        ChatGPTAnalysisResponse analysisResponse = this.chatGPTAnalysisService.analyze(claim, sources);
        if (analysisResponse.getError() != null)
            return new FactCheckResponse(null, analysisResponse.getError());

        return new FactCheckResponse(analysisResponse, null);
    }
}
