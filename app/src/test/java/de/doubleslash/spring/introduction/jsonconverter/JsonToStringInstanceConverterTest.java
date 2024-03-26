package de.doubleslash.spring.introduction.jsonconverter;


import com.fasterxml.jackson.core.JsonProcessingException;
import de.doubleslash.spring.introduction.controller.CarDealershipService;
import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.JsonStringToInstanceConverter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonToStringInstanceConverterTest {
    private final JsonStringToInstanceConverter converter = new JsonStringToInstanceConverter();

    @Test
    void givenValidCarJson_whenParsingJson_thenReturnCar() throws JsonProcessingException {
        final String newCarJson = "{\"brand\":\"TestBrand\",\"model\":\"TestModel\"}";
        System.out.println(converter.convert(newCarJson, Car.class));
        assertThat(converter.convert(newCarJson, Car.class)).isNotNull();
    }

    @Test
    void givenInvalidCarJson_whenParsingJson_thenThrowException() {
        final String newCarJson = "\"brand\":\"TestBrand\",\"model\":\"TestModel\"}";

        assertThrows(JsonProcessingException.class, () -> converter.convert(newCarJson, Car.class),
                CarDealershipService.CAR_JSON_PARSE_FAILURE_STRING);
    }
}
