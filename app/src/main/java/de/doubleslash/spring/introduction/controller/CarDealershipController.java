package de.doubleslash.spring.introduction.controller;

import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.CarCheckMappingRequest;
import de.doubleslash.spring.introduction.repository.CarRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@Slf4j
public class CarDealershipController {
    private final CarRepository repository;

    @GetMapping("/cars")
    public ResponseEntity<List<Car>> all() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/cars/{id}")
    public ResponseEntity<String> get(@Valid @NotNull @PathVariable long id) throws CarNotFoundException {
        Optional<Car> optionalCar = repository.findById(id);
        if (optionalCar.isEmpty()) {
            throw new CarNotFoundException(getErrorMessageForId(id));
        }

        return new ResponseEntity<>(optionalCar.get().toString(), HttpStatus.OK);
    }

    @PostMapping("/cars/add")
    public ResponseEntity<String> addCar(@Valid @NotNull @RequestBody Car car) {
        repository.save(car);
        String responseText = "Successfully added car %s %s".formatted(car.getBrand(), car.getModel());

        return new ResponseEntity<>(responseText, HttpStatus.OK);
    }

    @PostMapping("/cars/replace")
    public ResponseEntity<String> replaceCar(@Valid @RequestBody CarCheckMappingRequest mappingRequest) throws CarNotFoundException {
        Car firstCar = mappingRequest.firstCar;
        Car secondCar = mappingRequest.secondCar;

        if (firstCar.getId() == null || !repository.existsById(firstCar.getId())) {
            throw new CarNotFoundException(getErrorMessageForId(firstCar.getId()));
        }
        repository.deleteById(firstCar.getId());
        repository.save(secondCar);

        return new ResponseEntity<>("Replacement successful", HttpStatus.OK);
    }

    @DeleteMapping("/cars/{id}")
    public ResponseEntity<String> deleteCar(@Valid @NotNull @PathVariable Long id) throws CarNotFoundException {
        if (!repository.existsById(id)) {
            throw new CarNotFoundException();
        }
        repository.deleteById(id);

        return new ResponseEntity<>("Deletion successful", HttpStatus.OK);
    }

    @DeleteMapping("/cars/brand/{brand}")
    @Transactional
    public ResponseEntity<String> deleteCarByBrand(@Valid @NotNull @PathVariable String brand) {
        String responseText;
        List<Car> deleted = repository.deleteCarByBrand(brand);
        if (!deleted.isEmpty()) {
            responseText = "Successfully deleted %d car(s) of brand %s".formatted(deleted.size(), brand);
        } else {
            responseText = "No cars were deleted";
        }

        return new ResponseEntity<>(responseText, HttpStatus.OK);
    }

    private String getErrorMessageForId(Long id) {
        return "No car with requested id %d found".formatted(id);
    }
}