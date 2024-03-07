package de.doubleslash.spring.introduction.springcontexttests.controller;

import de.doubleslash.spring.introduction.controller.CarDealershipController;
import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.repository.CarRepository;
import de.doubleslash.spring.introduction.springcontexttests.setup.SpringInMemoryTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetCarInMemoryTest extends SpringInMemoryTest {

    @Resource
    private CarRepository repository;
    @Resource
    private CarDealershipController controller;

    @Test
    void givenCars_whenRequestingCars_thenGetCars() {
        final List<Car> carList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Car car = Car.builder().model("TestModel").brand("TestBrand").build();
            carList.add(car);
        }

        repository.saveAll(carList);

        assertThat(controller.all().getBody()).hasSameSizeAs(carList)
                .allMatch(car -> carList.stream().anyMatch(car1 -> car1.equals(car)));
    }

    @Test
    void givenCar_whenRequestingCar_thenGetCar() {
        final Car car = Car.builder().model("TestModel").brand("TestBrand").build();
        repository.save(car);
        final String expected = car.toString();
        assertThat(controller.get(car.getId()).getBody()).isEqualTo(expected);
    }

}