package de.doubleslash.spring.introduction;

import de.doubleslash.spring.introduction.model.Car;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class SpringIntroductionApplicationTest {

    private final RestTemplate template = new RestTemplate();

    @Test
    void contextLoads() {
    }

    /*@Test
    void givenCar_whenGetCarById_thenReturnCar() {
        final HttpEntity<MultiValueMap<String, Object>> multiValueMapHttpEntity = getMultiValueMapHttpEntity();

        template.postForEntity("http://localhost:9090/cars/add", multiValueMapHttpEntity, String.class);
    }*/

    @NotNull
    private static HttpEntity<MultiValueMap<String, Object>> getMultiValueMapHttpEntity() {
        final Car car = Car.builder().brand("TestBrand").model("TestModel").build();
        final byte[] imageOfNewCar = new byte[1];

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        final MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("newCarJson", car.toString());
        map.add("imageOfNewCar", imageOfNewCar);

        return new HttpEntity<>(map, httpHeaders);
    }

    @Test
    void givenCar_whenRequestingCar_thenReturnErrorMessageString() {
        assertThrows(HttpClientErrorException.class,
                () -> template.getForEntity("http://localhost:9090/cars/0", Car.class));
    }
}