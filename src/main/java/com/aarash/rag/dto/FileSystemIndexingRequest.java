package com.aarash.rag.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record FileSystemIndexingRequest(
        @NotBlank String path,
        List<String> labels) {

}
