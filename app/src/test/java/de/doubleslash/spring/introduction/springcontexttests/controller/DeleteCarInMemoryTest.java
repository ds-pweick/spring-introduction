package de.doubleslash.spring.introduction.springcontexttests.controller;

import de.doubleslash.spring.introduction.controller.CarDealershipController;
import de.doubleslash.spring.introduction.controller.CarModelOrBrandStringTooLongException;
import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.repository.CarRepository;
import de.doubleslash.spring.introduction.springcontexttests.setup.SpringInMemoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class DeleteCarInMemoryTest extends SpringInMemoryTest {

    @Autowired
    private CarRepository repository;
    @Autowired
    private CarDealershipController controller;

    @Test
    void givenCar_whenDeletingCar_thenDeleteCar() throws CarModelOrBrandStringTooLongException {
        Car car = Car.builder().model("TestModel").brand("TestBrand").build();
        controller.addCar(car);
        repository.deleteById(0L);
        assertThat(repository.existsById(0L)).isFalse();
    }

    @Test
    void givenCarsOfBrand_whenDeletingCarsByBrand_thenDeleteCars() {

        final List<Car> carList = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                Car car = Car.builder().model("TestModel%d".formatted(i)).brand("TestBrand%d".formatted(i)).build();
                carList.add(car);
            }
        }

        repository.saveAll(carList);
        controller.deleteCarByBrand("TestBrand0");

        assertThat(repository.findAll()).allMatch(car -> car.getBrand().equals("TestBrand1"))
                .allMatch(car -> carList.stream().anyMatch(car1 -> car1.equals(car)));

    }


}