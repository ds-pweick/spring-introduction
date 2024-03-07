package de.doubleslash.spring.introduction.springcontexttests.controller;

import de.doubleslash.spring.introduction.controller.CarDealershipController;
import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.CarCheckMappingRequest;
import de.doubleslash.spring.introduction.repository.CarRepository;
import de.doubleslash.spring.introduction.springcontexttests.setup.SpringInMemoryTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostCarInMemoryTest extends SpringInMemoryTest {
    @Resource
    private CarRepository repository;
    @Resource
    private CarDealershipController controller;

    @Test
    void givenCar_whenAddingCar_thenGetCar() {
        final Car car = Car.builder().model("TestModel").brand("TestBrand").build();

        controller.addCar(car);

        assertThat(repository.existsById(car.getId())).isTrue();
    }

    @Test
    void givenCars_whenRequestingCarReplacement_thenGetNewCar() {
        final Car firstCar = Car.builder().model("TestModel").brand("TestBrand").build();
        final Car secondCar = Car.builder().model("TestModel").brand("TestBrand").build();
        final CarCheckMappingRequest mappingRequest = CarCheckMappingRequest.builder()
                .firstCar(firstCar).secondCar(secondCar).build();

        controller.addCar(firstCar);
        controller.replaceCar(mappingRequest);

        assertThat(repository.existsById(firstCar.getId())).isFalse();
        assertThat(repository.existsById(secondCar.getId())).isTrue();
    }

}