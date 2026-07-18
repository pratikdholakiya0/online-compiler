package com.app.babycode.controller;

import com.app.babycode.dto.ExecutionResult;
import com.app.babycode.dto.SubmissionRequest;
import com.app.babycode.entity.Submission;
import com.app.babycode.service.CodeExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/submissions")
public class SubmissionController {
    private final CodeExecutionService executionService;

    @PostMapping
    public ResponseEntity<Submission> submit(@Valid @RequestBody SubmissionRequest body) throws Exception {
        return ResponseEntity.status(202).body(executionService.submitCode(body));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Submission> getSubmissionResult(
            @PathVariable UUID id
            ){
        return ResponseEntity.ok(executionService.getSubmission(id));
    }
}
