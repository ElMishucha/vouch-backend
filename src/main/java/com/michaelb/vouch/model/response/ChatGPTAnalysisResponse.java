//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.michaelb.vouch.model.response;

import com.michaelb.vouch.model.aspect.FactualClarificationAspect;
import com.michaelb.vouch.model.aspect.FinalVerdictAspect;
import com.michaelb.vouch.model.aspect.PersuasiveStrategiesAspect;
import com.michaelb.vouch.model.aspect.PropagandaAspect;
import com.michaelb.vouch.model.aspect.SourceSupportAspect;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatGPTAnalysisResponse {
    FinalVerdictAspect finalVerdict;
    FactualClarificationAspect factualClarification;
    SourceSupportAspect sourceSupport;
    PropagandaAspect propaganda;
    PersuasiveStrategiesAspect persuasiveStrategies;
    String error;

    public ChatGPTAnalysisResponse(String error) {
        this.error = error;
    }
}
