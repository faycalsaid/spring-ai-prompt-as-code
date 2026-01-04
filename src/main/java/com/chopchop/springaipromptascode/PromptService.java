package com.chopchop.springaipromptascode;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class PromptService {
    private final ResourceLoader resourceLoader;
    private final String promptFilePath;
    @Getter
	private PromptConfiguration promptConfiguration;

    @Autowired
    public PromptService(ResourceLoader resourceLoader, @Value("${prompt.file.path}") String promptFilePath) {
        this.resourceLoader = resourceLoader;
        this.promptFilePath = promptFilePath;
    }

    @PostConstruct
    public void loadPromptConfiguration() throws IOException {
        Resource resource = resourceLoader.getResource(promptFilePath);
        try (InputStream inputStream = resource.getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            promptConfiguration = objectMapper.readValue(inputStream, PromptConfiguration.class);
        }
    }
}
