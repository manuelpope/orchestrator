package com.example.orchestrator.controller;

import com.example.orchestrator.repo.Execution;
import com.example.orchestrator.service.OrchestratorService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/trigger")
@AllArgsConstructor
public class TriggerController {

    private OrchestratorService orchestratorService;

    @PostMapping("/{scriptName}")
    public Mono<Map<String, String>> executeScript(@PathVariable String scriptName) {


        Mono<Execution> executionMono = orchestratorService.executeScript(scriptName).subscribeOn(Schedulers.boundedElastic());

        return Mono.just(Map.of("message", "Script " + scriptName + " has been sent",
                        "status", "Queued"))
                .doOnSuccess(start -> executionMono.subscribe())
                .onErrorResume(e -> Mono.just(Map.of("error", e.getMessage())));

    }

    @GetMapping("/executions")
    public Flux<Execution> allExecutions() {


        return orchestratorService.allFetchRows();
    }


}
