package com.chopchop.springaipromptascode;

import java.util.Map;
import lombok.Data;

@Data
public class GenerationConfig {
    private float temperature;
    private float topP;
    private int maxOutputTokens;
    private String responseMimeType;
	private int thinkingBudget;
	private boolean enableThinkingConfigTought;
    private Map<String, String> safetySettings;
}
