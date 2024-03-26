package de.doubleslash.spring.introduction.repository;

import de.doubleslash.spring.introduction.model.CarImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CarImageRepository extends JpaRepository<CarImage, Long> {
    List<CarImage> findAllByAssociatedCarId(Long associatedCarId);
}