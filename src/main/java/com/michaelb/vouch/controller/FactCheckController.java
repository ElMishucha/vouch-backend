//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.michaelb.vouch.controller;

import com.michaelb.vouch.model.request.FactCheckRequest;
import com.michaelb.vouch.model.response.FactCheckResponse;
import com.michaelb.vouch.service.FactCheckService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/fact_check"})
@CrossOrigin
public class FactCheckController {
    private final FactCheckService factCheckService;

    public FactCheckController(FactCheckService factCheckService) {
        this.factCheckService = factCheckService;
    }

    @PostMapping
    public FactCheckResponse factCheck(@RequestBody FactCheckRequest request) {
        return this.factCheckService.factCheck(request.getClaim());
    }
}