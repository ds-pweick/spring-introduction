package de.doubleslash.spring.introduction.repository;

import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.CarCheckMappingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> all();

    Car get(long id);

    int replaceCar(CarCheckMappingRequest mappingRequest);

    int deleteCar(long id);

    int deleteCarByBrand(final String brand);

    //List<Car> deleteAllByDateBefore(final Instant expiration);

}