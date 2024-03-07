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

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@AllArgsConstructor
@Slf4j
public class CarDealershipController {
    private final CarRepository repository;

    @GetMapping("/cars")
    public ResponseEntity<List<Car>> all() {
        List<Car> carList = Collections.emptyList();
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        try {
            carList = repository.findAll();
            httpStatus = HttpStatus.OK;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ResponseEntity<>(carList, httpStatus);
    }

    @GetMapping("/cars/{id}")
    public ResponseEntity<String> get(@Valid @NotNull @PathVariable long id) {
        String responseText = "No car of id %d was found".formatted(id);
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        try {
            Car car = repository.findById(id).orElseThrow(NoSuchElementException::new);
            responseText = car.toString();
            httpStatus = HttpStatus.OK;
        } catch (NoSuchElementException e) {
            httpStatus = HttpStatus.BAD_REQUEST;
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return new ResponseEntity<>(responseText, httpStatus);
    }

    @PostMapping("/cars/add")
    public ResponseEntity<String> addCar(@Valid @NotNull @RequestBody Car car) {
        String responseText = "Adding car was unsuccessful";
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        try {
            repository.save(car);
            responseText = "Successfully added car";
            httpStatus = HttpStatus.OK;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return new ResponseEntity<>(responseText, httpStatus);
    }

    @PostMapping("/cars/replace")
    public ResponseEntity<Optional<String>> replaceCar(@Valid @RequestBody CarCheckMappingRequest mappingRequest) {
        Car firstCar = mappingRequest.firstCar;
        Car secondCar = mappingRequest.secondCar;

        String responseText = "No car of id %d was replaced".formatted(firstCar.getId());
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        try {
            if (!repository.existsById(firstCar.getId())) {
                responseText = "No car with requested id %d found".formatted(firstCar.getId());
                throw new NoSuchElementException(responseText);
            }
            repository.deleteById(firstCar.getId());
            repository.save(secondCar);
            responseText = "Successfully replaced car";
            httpStatus = HttpStatus.OK;
        } catch (NoSuchElementException e) {
            httpStatus = HttpStatus.BAD_REQUEST;
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return new ResponseEntity<>(Optional.of(responseText), httpStatus);
    }

    @DeleteMapping("/cars/{id}")
    public ResponseEntity<String> deleteCar(@Valid @NotNull @PathVariable Long id) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String responseText = "Deletion unsuccessful";
        try {
            if (!repository.existsById(id)) {
                responseText = "No car with requested id %d found".formatted(id);
                throw new NoSuchElementException(responseText);
            }
            repository.deleteById(id);
            httpStatus = HttpStatus.OK;
            responseText = "Deletion successful";
        } catch (NoSuchElementException e) {
            httpStatus = HttpStatus.BAD_REQUEST;
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return new ResponseEntity<>(responseText, httpStatus);
    }

    @DeleteMapping("/cars/brand/{brand}")
    @Transactional
    public ResponseEntity<String> deleteCarByBrand(@Valid @NotNull @PathVariable String brand) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String responseText = "Deletion unsuccessful";
        try {
            List<Car> deleted = repository.deleteCarByBrand(brand);
            httpStatus = HttpStatus.OK;
            if (!deleted.isEmpty()) {
                responseText = "Successfully deleted %d car(s) of brand %s".formatted(deleted.size(), brand);
            } else {
                responseText = "No cars were deleted";
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ResponseEntity<>(responseText, httpStatus);
    }

}