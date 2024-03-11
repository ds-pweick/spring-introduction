package de.doubleslash.spring.introduction.controller;

import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.CarCheckMappingRequest;
import de.doubleslash.spring.introduction.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarDealershipControllerTest {

    @Mock
    private CarRepository repository;

    @InjectMocks
    private CarDealershipController controller;

    @Test
    void givenValidRequestToAddCar_whenAddingCar_thenReturnSuccessMessageString() throws CarModelOrBrandStringTooLongException {
        final Car car = Car.builder().model("TestModel").brand("TestBrand").build();
        final String expected = "Successfully added car TestBrand TestModel";

        when(repository.save(car)).thenReturn(car);

        final ResponseEntity<String> result = controller.addCar(car);

        assertThat(result.hasBody()).isTrue();
        assertThat(result.getBody()).isEqualTo(expected);
    }

    @Test
    void givenInvalidRequestToAddCar_whenAddingCar_thenReturnErrorMessageString() {
        final Car car = Car.builder().model("TestModel".repeat(500)).brand("TestBrand".repeat(500)).build();
        final String expected = "Car model and/or brand name exceeds the character limit";

        assertThrows(CarModelOrBrandStringTooLongException.class, () -> controller.addCar(car), expected);
    }

    @Test
    void givenRequestToFetchAllCars_whenFetchingCars_thenReturnCarList() {
        final List<Car> expected = List.of(Car.builder().model("TestModel").brand("TestBrand").build());
        when(repository.findAll()).thenReturn(expected);

        final ResponseEntity<List<Car>> result = controller.all();

        assertThat(result.hasBody()).isTrue();
        assertThat(result.getBody()).isEqualTo(expected);
        verify(repository).findAll();
    }

    @Test
    void givenCarId_whenGetCarById_thenReturnCarAsString() throws CarNotFoundException {
        final Car expected = Car.builder().model("TestModel").brand("TestBrand").build();
        when(repository.findById(1L)).thenReturn(Optional.of(expected));

        final ResponseEntity<String> result = controller.get(1L);

        assertThat(result.hasBody()).isTrue();
        assertThat(result.getBody()).isEqualTo(expected.toString());
    }

    @Test
    void givenNonExistentCarId_whenGetCarById_thenReturnErrorMessageString() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CarNotFoundException.class, () -> controller.get(1L));
    }

    @Test
    void givenValidCarCheckMappingRequest_whenReplacingCar_thenReturnSuccessMessageString() throws CarNotFoundException, CarModelOrBrandStringTooLongException {
        final Car firstCar = Car.builder().id(1L).model("TestModel").brand("TestBrand").build();
        final Car secondCar = Car.builder().model("TestModel2").brand("TestBrand2").build();
        final CarCheckMappingRequest mappingRequest = CarCheckMappingRequest.builder()
                .firstCar(firstCar).secondCar(secondCar).build();
        final String expected = "Replacement successful";

        when(repository.existsById(firstCar.getId())).thenReturn(true);

        final ResponseEntity<String> result = controller.replaceCar(mappingRequest);

        assertThat(result.hasBody()).isTrue();
        assertThat(result.getBody()).isEqualTo(expected);
    }

    @Test
    void givenInvalidCarCheckMappingRequest_whenReplacingCar_thenReturnErrorMessageString() {
        final Car firstCar = Car.builder().id(1L).model("TestModel").brand("TestBrand").build();
        final Car secondCar = Car.builder().model("TestModel2").brand("TestBrand2").build();
        final CarCheckMappingRequest mappingRequest = CarCheckMappingRequest.builder()
                .firstCar(firstCar).secondCar(secondCar).build();
        final String expected = "No car with requested id 1 found";

        System.out.println(firstCar);
        System.out.println(secondCar);

        when(repository.existsById(firstCar.getId())).thenReturn(false);

        assertThrows(CarNotFoundException.class, () -> controller.replaceCar(mappingRequest), expected);
    }

    @Test
    void givenValidRequestToDeleteCar_whenDeletingCar_thenReturnSuccessMessageString() throws CarNotFoundException {
        final String expected = "Deletion successful";

        when(repository.existsById(1L)).thenReturn(true);

        final ResponseEntity<String> result = controller.deleteCar(1L);

        assertThat(result.hasBody()).isTrue();
        assertThat(result.getBody()).isEqualTo(expected);
    }

    @Test
    void givenInvalidRequestToDeleteCar_whenDeletingCar_thenReturnErrorMessageString() {
        final String expected = "No car with requested id 1 found";

        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(CarNotFoundException.class, () -> controller.deleteCar(1L), expected);
    }

    @Test
    void givenValidDeleteCarsByBrandRequest_whenDeletingCars_thenReturnCarList() {
        final String expected = "Successfully deleted 2 car(s) of brand VW";
        final Car firstCar = Car.builder().brand("VW").build();
        final Car secondCar = Car.builder().brand("VW").build();
        when(repository.deleteCarByBrand("VW")).thenReturn(List.of(firstCar, secondCar));

        final ResponseEntity<String> result = controller.deleteCarByBrand("VW");

        assertThat(result.hasBody()).isTrue();
        assertThat(result.getBody()).isEqualTo(expected);
    }

}