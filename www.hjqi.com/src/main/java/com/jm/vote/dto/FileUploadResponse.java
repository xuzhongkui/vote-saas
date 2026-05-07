package com.jm.vote.dto;

import lombok.Data;

@Data
public class FileUploadResponse {
    private String url;
    private String originalName;
    private Long size;
}

