package com.chopchop.springaipromptascode;

import lombok.Data;

@Data
public class PromptConfiguration {
    private String version;
    private String prompt;
    private GenerationConfig generationConfig;
}
