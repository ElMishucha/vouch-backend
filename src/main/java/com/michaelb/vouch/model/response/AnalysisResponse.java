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
import lombok.NoArgsConstructor;

@AllArgsConstructor
public class AnalysisResponse {
    public FinalVerdictAspect finalVerdict;
    public FactualClarificationAspect factualClarification;
    public SourceSupportAspect sourceSupport;
    public PropagandaAspect propaganda;
    public PersuasiveStrategiesAspect persuasiveStrategies;
    public String error;
}
