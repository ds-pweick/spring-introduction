package de.doubleslash.spring.introduction.controller;

import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.CarCheckMappingRequest;
import de.doubleslash.spring.introduction.model.ResponseTransfer;
import de.doubleslash.spring.introduction.repository.CarRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class CarDealershipController {
    private final CarRepository repository;

    @GetMapping
    @RequestMapping("/cars")
    public ResponseEntity<List<Car>> all() {
        List<Car> carList = Collections.emptyList();
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        try {
            carList = repository.all();
            httpStatus = HttpStatus.OK;
        } catch (Exception e) {
            printError(e.getMessage());
        }
        return new ResponseEntity<>(carList, httpStatus);
    }

    @GetMapping
    @RequestMapping("/cars/{id}")
    public ResponseEntity<String> get(@PathVariable long id) {
        Car car = repository.get(id);
        String responseText;
        try {
            responseText = car.toString();
        } catch (NullPointerException e) {
            printError(e.getMessage());
            responseText = "";
        }

        return new ResponseEntity<>(responseText, HttpStatus.OK);
    }

    @PostMapping
    @RequestMapping("/cars")
    @ResponseBody
    public ResponseEntity<Optional> replaceCar(@Valid @NotNull @RequestBody CarCheckMappingRequest mappingRequest) {
        //Car car = repository.replaceCar(mappingRequest);
        Optional<Car>

        String response;
        if (success == 0) response = "No car was replaced"; else response = "Car successfully replaced";
        ResponseTransfer responseTransfer = new ResponseTransfer(response);

        return new ResponseEntity<>(responseTransfer, httpStatus);
    }

    @DeleteMapping
    @RequestMapping("/cars/{id}")
    public ResponseEntity<Optional> deleteCar(@PathVariable long id) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        try {
            repository.deleteCar(id);
            httpStatus = HttpStatus.OK;
        } catch (Exception e) {
            printError(e.getMessage());
        }

        return new ResponseEntity<>(httpStatus);
    }

    @DeleteMapping
    @RequestMapping("/cars/brand/{brand}")
    public ResponseEntity<Optional> deleteCarByBrand(@PathVariable String brand) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        try {
            repository.deleteCarByBrand(brand);
            httpStatus = HttpStatus.OK;
        } catch (Exception e) {
            printError(e.getMessage());
        }
        return new ResponseEntity<>(httpStatus);
    }

    private void printError(String message) {
        System.out.printf("Exception has occurred: %s%n", message);
    }

}
