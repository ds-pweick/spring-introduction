package de.doubleslash.spring.introduction.controller;

import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.CarCheckMappingRequest;
import de.doubleslash.spring.introduction.model.JsonToCarConverter;
import de.doubleslash.spring.introduction.repository.CarRepository;
import io.minio.errors.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarDealershipControllerTest {

    @Mock
    private CarRepository repository;

    @InjectMocks
    private CarDealershipController controller;

    @Mock
    private JsonToCarConverter converter;

    @Test
    void givenValidRequestToAddCar_whenAddingCar_thenReturnSuccessMessageString() throws CarModelOrBrandStringInvalidException {
        final Car car = Car.builder().model("TestModel").brand("TestBrand").build();
        final String expected = CarDealershipController.ADD_CAR_SUCCESS_STRING.formatted("TestBrand", "TestModel");

        when(repository.save(car)).thenReturn(car);

        final ResponseEntity<String> result = controller.addCar(car);

        assertThat(result.hasBody()).isTrue();
        assertThat(result.getBody()).isEqualTo(expected);
    }

    @Test
    void givenInvalidRequestToAddCar_whenAddingCar_thenReturnErrorMessageString() {
        final Car car = Car.builder().model("TestModel".repeat(500)).brand("TestBrand".repeat(500)).build();
        final String expected = CarDealershipController.MODEL_OR_BRAND_INVALID_STRING;

        assertThrows(CarModelOrBrandStringInvalidException.class, () -> controller.addCar(car), expected);
    }

    @Test
    void givenValidRequestToAddCarImage_whenAddingCarImage_thenReturnSuccessMessageString() throws ServerException,
            InvalidFileUploadException, InsufficientDataException, ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
            InternalException, CarModelOrBrandStringInvalidException {

        final MockMultipartFile file =
                new MockMultipartFile("file", "TestTitle.png",
                        MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        final Car car = Car.builder().id(0L).model("TestModel").brand("TestBrand").build();
        final String carString = "{\"brand\":\"TestBrand\",\"model\":\"TestModel\"}";

        final String expected = CarDealershipController.FILE_UPLOAD_SUCCESS_STRING;

        when(repository.save(car)).thenReturn(car);
        when(converter.convert(carString)).thenReturn(car);

        final ResponseEntity<String> result = controller.addCarWithImage(file, carString);

        assertThat(result.getBody()).isEqualTo(expected);
    }

    @Test
    void givenInvalidRequestToAddCarImage_whenAddingCarImage_thenReturnErrorMessageString() {
        // file name >= 255 characters
        final MockMultipartFile firstFile =
                new MockMultipartFile("file", "TestTitle".repeat(500).concat(".png"),
                        MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        // file has "more than one" extension
        final MockMultipartFile secondFile =
                new MockMultipartFile("file", "TestTitle.exe.jpg",
                        MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        final String carString = "{\"brand\":\"TestBrand\",\"model\":\"TestModel\"}";
        final Car car = Car.builder().id(0L).model("TestModel").brand("TestBrand").build();

        final String expected = CarDealershipController.FILE_UPLOAD_FAILURE_STRING;

        when(repository.save(car)).thenReturn(car);
        when(converter.convert(carString)).thenReturn(car);

        assertThrows(InvalidFileUploadException.class, () -> controller.addCarWithImage(firstFile, carString), expected);
        assertThrows(InvalidFileUploadException.class, () -> controller.addCarWithImage(secondFile, carString), expected);
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
        final Car expected = Car.builder().id(1L).model("TestModel").brand("TestBrand").build();
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
    void givenValidCarCheckMappingRequest_whenReplacingCar_thenReturnSuccessMessageString() throws CarNotFoundException, CarModelOrBrandStringInvalidException {
        final Car firstCar = Car.builder().id(1L).model("TestModel").brand("TestBrand").build();
        final Car secondCar = Car.builder().model("TestModel2").brand("TestBrand2").build();
        final CarCheckMappingRequest mappingRequest = CarCheckMappingRequest.builder()
                .firstCar(firstCar).secondCar(secondCar).build();
        final String expected = CarDealershipController.REPLACE_CAR_SUCCESS_STRING;

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
        final String expected = CarDealershipController.CAR_NOT_FOUND_STRING.formatted(1L);

        System.out.println(firstCar);
        System.out.println(secondCar);

        when(repository.existsById(firstCar.getId())).thenReturn(false);

        assertThrows(CarNotFoundException.class, () -> controller.replaceCar(mappingRequest), expected);
    }

    @Test
    void givenValidRequestToDeleteCar_whenDeletingCar_thenReturnSuccessMessageString() throws CarNotFoundException {
        final String expected = CarDealershipController.DELETE_CAR_SUCCESS_STRING;

        when(repository.existsById(1L)).thenReturn(true);

        final ResponseEntity<String> result = controller.deleteCar(1L);

        assertThat(result.hasBody()).isTrue();
        assertThat(result.getBody()).isEqualTo(expected);
    }

    @Test
    void givenInvalidRequestToDeleteCar_whenDeletingCar_thenReturnErrorMessageString() {
        final String expected = CarDealershipController.CAR_NOT_FOUND_STRING.formatted(1L);

        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(CarNotFoundException.class, () -> controller.deleteCar(1L), expected);
    }

    @Test
    void givenValidDeleteCarsByBrandRequest_whenDeletingCars_thenReturnCarList() {
        final String expected = CarDealershipController.DELETE_CAR_BY_BRAND_SUCCESS_STRING.formatted(2, "VW");
        final Car firstCar = Car.builder().brand("VW").build();
        final Car secondCar = Car.builder().brand("VW").build();
        when(repository.deleteCarByBrand("VW")).thenReturn(List.of(firstCar, secondCar));

        final ResponseEntity<String> result = controller.deleteCarByBrand("VW");

        assertThat(result.hasBody()).isTrue();
        assertThat(result.getBody()).isEqualTo(expected);
    }

}