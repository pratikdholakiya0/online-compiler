package com.app.babycode.entity;

import com.app.babycode.dto.ExecutionResult;
import com.app.babycode.enums.Language;
import com.app.babycode.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {
    private UUID id;
    private Language language;
    private String sourceCode;
    private String stdin;
    private ExecutionResult result;
    private SubmissionStatus status;
    private LocalDateTime submittedAt;
}
