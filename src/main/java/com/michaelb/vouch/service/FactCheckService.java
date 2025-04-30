//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.michaelb.vouch.service;

import com.google.gson.Gson;
import com.michaelb.vouch.model.Source;
import com.michaelb.vouch.model.aspect.SourceSupportAspect;
import com.michaelb.vouch.model.response.AnalysisResponse;
import com.michaelb.vouch.model.response.FactCheckResponse;

import java.util.ArrayList;
import java.util.List;

import com.michaelb.vouch.model.response.SummaryResponse;
import com.michaelb.vouch.model.response.WebSearchResponse;
import org.springframework.stereotype.Service;

@Service
public class FactCheckService {
    private final SummaryService summaryService;
    private final WebSearchService webSearchService;
    private final AnalysisService analysisService;
    private final Gson gson = new Gson();

    FactCheckService(SummaryService summaryService, WebSearchService webSearchService, AnalysisService analysisService) {
        this.summaryService = summaryService;
        this.webSearchService = webSearchService;
        this.analysisService = analysisService;
    }

    public FactCheckResponse factCheck(String claim) {
        System.out.println("Fact-Checking claim: " + claim);

        // Summary
        SummaryResponse summaryResponse = this.summaryService.summarizeClaimForSearch(claim);

        if (summaryResponse.error != null)
            return new FactCheckResponse(null, summaryResponse.error);

        String claimSummary = summaryResponse.claimSummary;
        System.out.println("Claim Summary: " + claimSummary);

        // WebSearch
        WebSearchResponse webSearchResponse = this.webSearchService.webSearch(claimSummary);

        if (webSearchResponse.error != null)
            return new FactCheckResponse(null, webSearchResponse.error);

        List<Source> sources = webSearchResponse.sources;

        // Analysis
        AnalysisResponse analysisResponse = this.analysisService.analyze(claim, sources);

        if (analysisResponse.error != null) {
            return new FactCheckResponse(null, analysisResponse.error);
        }

        // Response
        return new FactCheckResponse(analysisResponse, null);
    }
}
