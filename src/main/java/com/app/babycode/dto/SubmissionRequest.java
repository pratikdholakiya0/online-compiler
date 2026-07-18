package com.app.babycode.dto;

import com.app.babycode.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmissionRequest {
    @NotNull(message = "Language is required!")
    private Language language;

    @NotBlank(message = "Source code is required!")
    private String sourceCode;

    @Size(max = 8_192, message = "Standard input size exceeds limit!")
    private String stdIn;
}
