package com.aarash.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record URLIndexingRequest(
        @NotBlank @Pattern(regexp = "^(?i)(http|https)://.*$") String url,
        List<String> labels) {

}
