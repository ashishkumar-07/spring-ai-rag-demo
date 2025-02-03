package com.aarash.rag.dto;

public record IndexingResponse(IndexingStatus status, String error) {
}

public enum IndexingStatus{
    SUCCESS, ALREADY_PROCESSED, FAILED
}
