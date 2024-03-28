package de.doubleslash.spring.introduction.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JsonStringToInstanceConverter {
    private final ObjectMapper objectMapper;

    public JsonStringToInstanceConverter() {
        objectMapper = JsonMapper.builder().addModule(new JavaTimeModule())
                .build();
    }

    public <T> T convert(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }
}