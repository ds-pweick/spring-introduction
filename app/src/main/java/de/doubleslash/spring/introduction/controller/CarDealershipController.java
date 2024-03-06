package de.doubleslash.spring.introduction.controller;

import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.CarCheckMappingRequest;
import de.doubleslash.spring.introduction.repository.CarRepository;
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
    public ResponseEntity<String> get(@PathVariable long id) {
        String responseText = "No car of this id was found!";
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        try {
            Car car = repository.findById(id).orElseThrow(NoSuchElementException::new);
            responseText = car.toString();
            httpStatus = HttpStatus.OK;
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
        }

        return new ResponseEntity<>(responseText, httpStatus);
    }

    @PostMapping("/cars")
    @ResponseBody
    public ResponseEntity<Optional<String>> replaceCar(@Valid @NotNull @RequestBody CarCheckMappingRequest mappingRequest) {
        Car firstCar = mappingRequest.firstCar;
        Car secondCar = mappingRequest.secondCar;

        String responseText = "No car of this id was found!";
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        try {
            repository.deleteById(firstCar.getId());
            repository.save(secondCar);
            responseText = "Successfully replaced cars";
            httpStatus = HttpStatus.OK;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return new ResponseEntity<>(Optional.of(responseText), httpStatus);
    }

    @DeleteMapping("/cars/{id}")
    public ResponseEntity<String> deleteCar(@PathVariable Long id) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String responseText = "Deletion unsuccessful";
        try {
            repository.deleteById(id);
            httpStatus = HttpStatus.OK;
            responseText = "Deletion successful";
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return new ResponseEntity<>(responseText, httpStatus);
    }

    @DeleteMapping("/cars/brand/{brand}")
    public ResponseEntity<String> deleteCarByBrand(@PathVariable String brand) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String responseText = "Deletion unsuccessful";
        try {
            List<Car> deleted = repository.deleteCarByBrand(brand);
            httpStatus = HttpStatus.OK;
            if (!deleted.isEmpty()) {
                responseText = "Successfully deleted %d cars.".formatted(deleted.size());
            } else {
                responseText = "No cars were deleted.";
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ResponseEntity<>(responseText, httpStatus);
    }

}
