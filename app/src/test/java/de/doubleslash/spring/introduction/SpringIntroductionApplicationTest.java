package de.doubleslash.spring.introduction;

import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.CarImage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class SpringIntroductionApplicationTest {

    private final RestTemplate template = new RestTemplate();
    private final String json = "{\"brand\":\"TestBrand\",\"model\":\"TestModel\"}";
    private final Pair<ContentDisposition, byte[]> carMultipart = Pair.of(ContentDisposition
            .builder("form-data")
            .name("car")
            .build(), json.getBytes());
    private final Pair<ContentDisposition, byte[]> fileMultipart = Pair.of(ContentDisposition
            .builder("form-data")
            .name("file")
            .filename("TestTitle.png")
            .build(), new byte[1]);
    private final HttpEntity<MultiValueMap<String, Object>> multiPartHttpEntity =
            getMultiPartHttpEntity(List.of(carMultipart, fileMultipart), List.of("car", "file"));


    @NotNull
    private static HttpEntity<MultiValueMap<String, Object>> getMultiPartHttpEntity(List<Pair<ContentDisposition,
            byte[]>> partsList, List<String> parameterNames) {
        List<HttpEntity<byte[]>> entityList = new ArrayList<>();

        partsList.forEach(part -> {
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add(HttpHeaders.CONTENT_DISPOSITION, part.getFirst().toString());
            entityList.add(new HttpEntity<>(part.getSecond(), multiValueMap));
        });

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        IntStream.range(0, parameterNames.size()).forEach(i ->
                body.add(parameterNames.get(i), entityList.get(i))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(body, headers);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void givenCar_whenAddingCarAndImage_thenAddSuccessfully() {
        ResponseEntity<Car> carResponseEntity = template.postForEntity("http://localhost:9090/cars/add",
                multiPartHttpEntity, Car.class);

        assertThat(carResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Car car = carResponseEntity.getBody();

        assertThat(car).isNotNull();

        final Long id = car.getId();

        ResponseEntity<Car> carResponseEntity1 = template.getForEntity("http://localhost:9090/cars/" + id, Car.class);

        assertThat(carResponseEntity1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(carResponseEntity1.getBody().equals(car)).isTrue();
    }

    @Test
    void givenCar_whenHeavyAccessLoadOnEndpoint_ensureConsistentResponses() {
        ResponseEntity<Car> carResponseEntity = template.postForEntity("http://localhost:9090/cars/add",
                multiPartHttpEntity, Car.class);

        assertThat(carResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Car car = carResponseEntity.getBody();

        assertThat(car).isNotNull();

        final Long id = car.getId();

        Arrays.stream(new int[100000]).parallel().forEach(i -> {
            ResponseEntity<Car> carResponseEntity1 = template.getForEntity("http://localhost:9090/cars/" + id, Car.class);
            assertThat(carResponseEntity1.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(carResponseEntity1.getBody().equals(car)).isTrue();
        });
    }

    @Test
    void givenCar_whenRequestingNonExistentCar_thenReturnErrorMessageString() {
        assertThrows(HttpClientErrorException.class,
                () -> template.getForEntity("http://localhost:9090/cars/0", Car.class));
    }

    @Test
    void givenCarList_whenFetchingAllCars_thenReturnCarList() {
        IntStream.range(0, 5).forEach(i ->
                template.postForEntity("http://localhost:9090/cars/add", multiPartHttpEntity, String.class));

        final ResponseEntity<Car[]> entity = template.getForEntity("http://localhost:9090/cars", Car[].class);
        Car[] carList = entity.getBody();

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(carList).isNotNull().isNotEmpty();
    }

    @Test
    void givenCar_whenFetchingCarById_thenReturnCarWithMatchingCarImageList() {
        ResponseEntity<Car> carResponseEntity = template.postForEntity("http://localhost:9090/cars/add",
                multiPartHttpEntity, Car.class);

        assertThat(carResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Car car = carResponseEntity.getBody();

        assertThat(car).isNotNull();

        final Long id = car.getId();

        ResponseEntity<Car> carResponseEntity1 = template.getForEntity("http://localhost:9090/cars/" + id, Car.class);

        assertThat(carResponseEntity1.getStatusCode()).isEqualTo(HttpStatus.OK);

        Car car1 = carResponseEntity1.getBody();

        assertThat(car1).isNotNull();

        List<CarImage> carImageList = car1.getCarImageList();

        assertThat(carImageList.size()).isEqualTo(1);

        ResponseEntity<byte[]> entity1 =
                template.getForEntity("http://localhost:9090/images/" + carImageList.get(0), byte[].class);

        assertThat(entity1.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void givenCar_whenReplacingCar_thenPerformSuccessfulReplacement() {
        ResponseEntity<Car> oldCarResponseEntity =
                template.postForEntity("http://localhost:9090/cars/add", multiPartHttpEntity, Car.class);

        assertThat(oldCarResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        final Car oldCar = oldCarResponseEntity.getBody();

        assertThat(oldCar).isNotNull();

        final Long id = oldCar.getId();

        final Pair<ContentDisposition, byte[]> oldIdMultipart = Pair.of(ContentDisposition
                .builder("form-data")
                .name("oldId")
                .build(), id.toString().getBytes());

        final HttpEntity<MultiValueMap<String, Object>> newCarMultipartHttpEntity =
                getMultiPartHttpEntity(List.of(oldIdMultipart, carMultipart, fileMultipart), List.of("oldCarId",
                        "secondCar",
                        "file"));

        ResponseEntity<Car> newCarResponseEntity =
                template.postForEntity("http://localhost:9090/cars/replace", newCarMultipartHttpEntity, Car.class);

        assertThat(newCarResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        final Car newCar = newCarResponseEntity.getBody();

        assertThat(newCar).isNotNull();

        final Long newCarId = newCar.getId();

        final ResponseEntity<Car> carResponseEntity2 =
                template.getForEntity("http://localhost:9090/cars/" + newCarId, Car.class);
        assertThat(carResponseEntity2.getStatusCode()).isEqualTo(HttpStatus.OK);

        final Car newCar1 = newCarResponseEntity.getBody();

        assertThat(newCar1).isNotNull().hasFieldOrPropertyWithValue("id", newCarId);
    }

    @Test
    void givenCar_whenDeletingCar_thenPerformSuccessfulDeletion() {
        ResponseEntity<Car> carResponseEntity = template.postForEntity("http://localhost:9090/cars/add",
                multiPartHttpEntity, Car.class);

        assertThat(carResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Car car = carResponseEntity.getBody();

        assertThat(car).isNotNull();

        final Long id = car.getId();

        template.delete("http://localhost:9090/cars/" + id, Car.class);
    }

    @Test
    void givenCar_whenDeletingCarByBrand_thenPerformSuccessfulDeletion() {
        final String jsonBrandToStay = "{\"brand\":\"TestBrandToStay\",\"model\":\"TestModel1\"}";

        final Pair<ContentDisposition, byte[]> carMultipartBrandToStay = Pair.of(ContentDisposition
                .builder("form-data")
                .name("car")
                .build(), jsonBrandToStay.getBytes());

        final HttpEntity<MultiValueMap<String, Object>> multiPartHttpEntityBrandToStay =
                getMultiPartHttpEntity(List.of(carMultipartBrandToStay, fileMultipart), List.of("car", "file"));

        IntStream.range(0, 5).forEach(i ->
                template.postForEntity("http://localhost:9090/cars/add", multiPartHttpEntity, String.class));
        IntStream.range(0, 5).forEach(i ->
                template.postForEntity("http://localhost:9090/cars/add", multiPartHttpEntityBrandToStay, String.class));

        final ResponseEntity<Car[]> allCarsResponseEntity = template.getForEntity("http://localhost:9090/cars", Car[].class);
        Car[] allCarsList = allCarsResponseEntity.getBody();

        assertThat(allCarsResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(allCarsList).isNotNull().isNotEmpty();

        template.delete("http://localhost:9090/cars/brand/TestBrand");

        ResponseEntity<Car[]> carsToStayResponseEntity = template.getForEntity("http://localhost:9090/cars", Car[].class);
        Car[] carsToStayCarList = carsToStayResponseEntity.getBody();

        assertThat(carsToStayResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(carsToStayCarList).isNotNull().hasSize(5);
        assertThat(carsToStayCarList).allMatch(car -> car.getBrand().equals("TestBrandToStay"));
    }


}