package de.doubleslash.spring.introduction.repository;

import de.doubleslash.spring.introduction.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> deleteCarByBrand(final String brand);

    List<Car> deleteAllByDateBefore(final Instant expiration);

}