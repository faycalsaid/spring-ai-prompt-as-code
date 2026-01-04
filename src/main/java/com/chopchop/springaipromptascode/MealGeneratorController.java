package com.chopchop.springaipromptascode;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import com.google.genai.types.HarmBlockThreshold;
import com.google.genai.types.HarmCategory;
import com.google.genai.types.Part;
import com.google.genai.types.SafetySetting;
import com.google.genai.types.Schema;
import com.google.genai.types.ThinkingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/generate-meal")
public class MealGeneratorController {
	final Logger logger = LoggerFactory.getLogger(MealGeneratorController.class);
	private final String projectId;
	private final String location;
	private final String modelId;
	private final PromptService promptService;

	@Autowired
	public MealGeneratorController(@Value("${vertex-ai.project-id}") final String projectId,
								   @Value("${vertex-ai.location}") final String location,
								   @Value("${vertex-ai.model-id}") final String modelId,
								   final PromptService promptService) {
		this.projectId = projectId;
		this.location = location;
		this.modelId = modelId;
		this.promptService = promptService;
	}
	
	@GetMapping()
	public ResponseEntity<String> generateMeal() {
		PromptConfiguration promptConfig = promptService.getPromptConfiguration();
		GenerationConfig generationConfig = promptConfig.getGenerationConfig();

		Client modelClient = buildClient();
		Content systemInstruction = Content.fromParts(Part.fromText(promptConfig.getPrompt()));
		List<SafetySetting> safetySettings = buildSafetySettings(generationConfig);
		ThinkingConfig thinkingConfig = ThinkingConfig.builder()
				.thinkingBudget(generationConfig.getThinkingBudget())
				.includeThoughts(generationConfig.isEnableThinkingConfigTought())
				.build();
		GenerateContentConfig generateContentConfig = buildGenerationConfig(systemInstruction, null, thinkingConfig, safetySettings, generationConfig);
		GenerateContentResponse generatedContent = modelClient.models.generateContent(modelId, "", generateContentConfig);

		logger.atInfo().log("Content successully generated, total token count {}",
				generatedContent.usageMetadata().flatMap(GenerateContentResponseUsageMetadata::totalTokenCount));
		String recipes = generatedContent.text();
		return ResponseEntity.ok(recipes);
	}

	private Client buildClient() {
		return Client.builder()
				.project(projectId)
				.location(location)
				.vertexAI(true)
				.build();
	}

	private List<SafetySetting> buildSafetySettings(final GenerationConfig config) {
		if (config == null || config.getSafetySettings() == null) {
			return List.of();
		}

		return config.getSafetySettings().entrySet().stream()
				.map(entry -> SafetySetting.builder()
						.category(HarmCategory.Known.valueOf(entry.getKey()))
						.threshold(HarmBlockThreshold.Known.valueOf(entry.getValue()))
						.build())
				.collect(Collectors.toList());
	}

	private GenerateContentConfig buildGenerationConfig(final Content systemInstructions, final Schema responseSchema, final ThinkingConfig thinkingConfig, final List<SafetySetting> safetySettings, final GenerationConfig generationConfig) {
		return GenerateContentConfig.builder()
				.systemInstruction(systemInstructions)
				// TODO do we need to put all fields in response schema or just the root ?
				//				.responseSchema(responseSchema)
				.thinkingConfig(thinkingConfig)
				.safetySettings(safetySettings)
				.maxOutputTokens(generationConfig.getMaxOutputTokens())
				.temperature(generationConfig.getTemperature())
				.topP(generationConfig.getTopP())
				.responseMimeType(generationConfig.getResponseMimeType())
				.build();
	}
}