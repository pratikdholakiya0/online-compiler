package com.app.babycode.dto;

import com.app.babycode.enums.Verdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecutionResult {
    private Verdict status;
    private String output;
    private String error;
}
