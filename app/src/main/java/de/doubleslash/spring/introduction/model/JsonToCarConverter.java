package de.doubleslash.spring.introduction.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JsonToCarConverter implements Converter<String, Car> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Car convert(@NotNull String json) {
        Car car = new Car();
        try {
            car = objectMapper.readValue(json, Car.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert incoming json to car object");
        }

        return car;
    }
}