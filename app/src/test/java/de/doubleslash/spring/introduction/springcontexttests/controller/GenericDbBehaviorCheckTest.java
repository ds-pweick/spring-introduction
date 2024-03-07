package de.doubleslash.spring.introduction.springcontexttests.controller;

import de.doubleslash.spring.introduction.controller.CarDealershipController;
import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.repository.CarRepository;
import de.doubleslash.spring.introduction.springcontexttests.setup.SpringInMemoryTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericDbBehaviorCheckTest extends SpringInMemoryTest {

    @Resource
    private CarRepository repository;

    @Resource
    private CarDealershipController controller;

    @Test
    void givenDatabase_whenHeavyAccessLoad_thenCheckIfPerformanceConsistent() {
        final List<Car> carList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Car car = Car.builder().model("TestModel").brand("TestBrand").build();
            carList.add(car);
        }

        repository.saveAll(carList);

        Arrays.stream(new int[10000]).parallel().forEach(i -> {
            List<Car> result = controller.all().getBody();
            assertThat(result).isNotEmpty().hasSameSizeAs(carList);
            assertThat(result).allMatch(car -> carList.stream().anyMatch(car1 -> car1.equals(car)));
        });
    }

}