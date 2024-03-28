package de.doubleslash.spring.introduction.datajpatest;

import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
class PersistCarTest {
    private final Car car = Car.builder().model("TestModel").brand("TestBrand").build();
    @Autowired
    private CarRepository carRepository;

    private Car saved;

    @BeforeEach
    void cleanUpDb() {
        carRepository.deleteAllInBatch();
        saved = carRepository.save(car);
    }

    @Test
    void givenCar_whenRequestingCar_thenFindCarById() {
        Optional<Car> optionalCar = carRepository.findById(saved.getId());

        assertThat(optionalCar).isPresent();
        assertThat(optionalCar.get().equals(saved)).isTrue();
    }

    @Test
    void givenCar_whenDeletingCar_thenIsDeleted() {
        carRepository.deleteById(saved.getId());
        assertThat(carRepository.findById(saved.getId())).isEmpty();
    }

    /*@Test
    void givenCarAndImage_whenRequestingCar_thenFindCarAndImageById() {
        CarImage carImage = carImageRepository.save(CarImage.builder().associatedCar(saved).imageObjectName("TestTitle")
                .build());

        saved.setCarImageList(List.of(carImage));

        carRepository.deleteById(saved.getId());

        assertThat(carImageRepository.findById(carImage.getId())).isEmpty();
    }*/


}
