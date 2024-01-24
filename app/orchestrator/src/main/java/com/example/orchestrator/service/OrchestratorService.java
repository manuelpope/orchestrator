package com.example.orchestrator.service;

// OrchestratorService.java


import com.example.orchestrator.repo.Execution;
import com.example.orchestrator.repo.IExecutionRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

@Service
@AllArgsConstructor
@Slf4j
public class OrchestratorService {


    private IExecutionRepository executionRepository;

    private static String limitarLongitud(String texto, int longitudMaxima) {
        if (texto.length() <= longitudMaxima) {
            return texto;  // No es necesario truncar
        } else {
            return texto.substring(0, longitudMaxima);
        }
    }

    @Transactional
    public Mono<Execution> executeScript(String scriptName) {
        return Mono.fromCallable(() -> {
            Execution execution = new Execution();
            execution.setScriptName(scriptName);
            execution.setStatus("Started");
            String scriptPath = "scripts/" + scriptName + ".py";
            String path = obtenerRutaRelativa(scriptPath);


            if (Objects.isNull(path)) {

                execution.setStatus("Error");
                execution.setError("Path does not exist");

            }
            Execution executionSaved = executionRepository.save(execution);

            String status = executePythonScript(path, execution);
            executionSaved.setStatus(status);

            log.info("results:: " + executionSaved.toString());
            return executionRepository.save(executionSaved);
        });
    }

    private String executePythonScript(String scriptPath, Execution execution) {


        Process process = null;
        int exitCode = 0;

        try {
            process = new ProcessBuilder("python3", scriptPath).start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                var strError = sb.toString();
                if (!Strings.isEmpty(strError)) {
                    log.error(strError);

                    execution.setError(limitarLongitud(sb.toString(), 240));


                }
            }

            exitCode = process.waitFor();

        } catch (Exception e) {

            log.error(e.getMessage());
        }
        if (exitCode == 0) {
            return "Success";

        }

        return "Failed";
    }

    private String obtenerRutaRelativa(String relativePath) {
        ClassPathResource resource = new ClassPathResource(relativePath);

        File file = null;
        try {
            file = resource.getFile();
        } catch (IOException e) {

            log.error(e.getMessage());

            return null;
        }

        return file.getAbsolutePath();
    }

    public Flux<Execution> allFetchRows() {

        return Flux.fromIterable(executionRepository.findAll());
    }
}
