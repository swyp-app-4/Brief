package com.brife.news.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties(prefix = "vertex.ai")
@Getter
@Setter
public class VertexAiProperties {
    @NotBlank private String projectId;
    @NotBlank private String region;
    @NotBlank private String model;

    public String getEndpointUrl() {
        return String.format(
            "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent",
            region, projectId, region, model
        );
    }
}
