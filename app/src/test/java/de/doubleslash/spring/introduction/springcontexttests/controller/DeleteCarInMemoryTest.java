package de.doubleslash.spring.introduction.springcontexttests.controller;

import de.doubleslash.spring.introduction.controller.CarDealershipController;
import de.doubleslash.spring.introduction.controller.CarModelOrBrandStringInvalidException;
import de.doubleslash.spring.introduction.controller.InvalidFileRequestException;
import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.repository.CarRepository;
import de.doubleslash.spring.introduction.springcontexttests.setup.SpringInMemoryTest;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class DeleteCarInMemoryTest extends SpringInMemoryTest {

    @Autowired
    private CarRepository repository;
    @Autowired
    private CarDealershipController controller;

    @Test
    void givenCar_whenDeletingCar_thenDeleteCar() throws CarModelOrBrandStringInvalidException, InvalidFileRequestException {
        final String carString = "{\"brand\":\"TestBrand\",\"model\":\"TestModel\"}";
        final MockMultipartFile file = new MockMultipartFile("file", "TestTitle.png",
                MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        controller.addCar(carString, file);
        repository.deleteById(0L);
        assertThat(repository.existsById(0L)).isFalse();
    }

    @Test
    void givenCarsOfBrand_whenDeletingCarsByBrand_thenDeleteCars() throws MinioException, IOException,
            NoSuchAlgorithmException, InvalidKeyException {

        final List<Car> carList = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                Car car = Car.builder().model("TestModel%d".formatted(i)).brand("TestBrand%d".formatted(i))
                        .imageObjectName("TestImage.png").build();
                carList.add(car);
            }
        }

        repository.saveAll(carList);
        controller.deleteCarByBrand("TestBrand0");

        assertThat(repository.findAll()).allMatch(car -> car.getBrand().equals("TestBrand1"));
    }
}