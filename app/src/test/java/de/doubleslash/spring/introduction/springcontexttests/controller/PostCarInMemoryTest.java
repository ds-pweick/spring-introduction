package de.doubleslash.spring.introduction.springcontexttests.controller;

import de.doubleslash.spring.introduction.controller.CarDealershipController;
import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.repository.CarRepository;
import de.doubleslash.spring.introduction.springcontexttests.setup.SpringInMemoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class PostCarInMemoryTest extends SpringInMemoryTest {
    @Autowired
    private CarRepository repository;
    @Autowired
    private CarDealershipController controller;

    @Test
    void givenCar_whenAddingCar_thenGetCar() {
        final Car car = Car.builder().model("TestModel").brand("TestBrand").imageObjectName("TestImage.png").build();

        repository.save(car);

        assertThat(repository.existsById(car.getId())).isTrue();
    }

    @Test
    void givenCars_whenRequestingCarReplacement_thenGetNewCar() throws Exception {

        final Car car = Car.builder().model("TestModel").brand("TestBrand").imageObjectName("TestImage.png").build();
        final MockMultipartFile file = new MockMultipartFile("file", "TestTitle.png",
                MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        Car savedCar = repository.save(car);
        controller.replaceCar(savedCar.getId(), car.toString(), file);
        assertThat(repository.existsById(savedCar.getId())).isFalse();
    }

}