package de.doubleslash.spring.introduction.repository;

import de.doubleslash.spring.introduction.model.CarImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarImageRepository extends JpaRepository<CarImage, Long> {
    List<CarImage> findAllByAssociatedCarId(Long associatedCarId);
}