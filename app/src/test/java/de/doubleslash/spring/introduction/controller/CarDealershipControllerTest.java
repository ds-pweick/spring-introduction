package de.doubleslash.spring.introduction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.JsonStringToInstance;
import de.doubleslash.spring.introduction.model.MinIoFileHandler;
import de.doubleslash.spring.introduction.repository.CarImageRepository;
import de.doubleslash.spring.introduction.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarDealershipControllerTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarImageRepository carImageRepository;

    @InjectMocks
    private CarDealershipController controller;

    @Mock
    private JsonStringToInstance converter;

    @Mock
    private MinIoFileHandler fileHandler;

    @Test
    void givenValidRequestToAddCar_whenAddingCar_thenReturnSuccessMessageString() throws CarModelOrBrandStringInvalidException,
            InvalidFileRequestException, JsonProcessingException {
        final String carString = "{\"brand\":\"TestBrand\",\"model\":\"TestModel\"}";
        final MockMultipartFile file = new MockMultipartFile("file", "TestTitle.png",
                MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        final Car car = Car.builder().id(1L).model("TestModel").brand("TestBrand").build();

        final String expected = CarDealershipController.ADD_CAR_SUCCESS_STRING.formatted("TestBrand", "TestModel");

        when(converter.convert(carString, Car.class)).thenReturn(car);

        final ResponseEntity<String> result = controller.addCar(carString, file);

        assertThat(result.getBody()).isEqualTo(expected);
    }

    @Test
    void givenInvalidRequestToAddCar_whenValidatingCarBrand_thenReturnErrorMessageString() {
        final String carString = "{\"brand\":\"" + "TestBrand".repeat(500) + "\",\"model\":\"TestModel\"}";
        final MockMultipartFile file = new MockMultipartFile("file", "TestTitle.png",
                MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        assertThrows(CarModelOrBrandStringInvalidException.class, () -> controller.addCar(carString, file),
                CarDealershipController.MODEL_OR_BRAND_INVALID_STRING);
    }

    @Test
    void givenInvalidRequestToAddCar_whenValidatingMultipartFile_thenReturnErrorMessageString() throws JsonProcessingException {
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

        final String expected = CarDealershipController.FILE_UPLOAD_INVALID_NAME_FAILURE_STRING;

        when(converter.convert(carString, Car.class)).thenReturn(car);

        assertThrows(InvalidFileRequestException.class, () -> controller.addCar(carString, firstFile), expected);
        assertThrows(InvalidFileRequestException.class, () -> controller.addCar(carString, secondFile), expected);
    }

    @Test
    void givenRequestToFetchAllCars_whenFetchingAllCars_thenReturnCarList() {
        final List<Car> expected = List.of(Car.builder().model("TestModel").brand("TestBrand").build());
        when(carRepository.findAll()).thenReturn(expected);

        final ResponseEntity<List<Car>> result = controller.allCars();

        assertThat(result.hasBody()).isTrue();
        assertThat(result.getBody()).isEqualTo(expected);
    }

    @Test
    void givenCarId_whenGetCarById_thenReturnCarAsString() throws CarNotFoundException {
        final Car expected = Car.builder().id(1L).model("TestModel").brand("TestBrand").build();
        when(carRepository.findById(1L)).thenReturn(Optional.of(expected));

        final Car result = controller.get(1L).getBody();

        assertThat(result).isNotNull();
        assertThat(result.equals(expected)).isTrue();
    }

    @Test
    void givenNonExistentCarId_whenGetCarById_thenReturnErrorMessageString() {
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CarNotFoundException.class, () -> controller.get(1L));
    }

    @Test
    void givenValidReplacementRequest_whenReplacingCar_thenReturnSuccessMessageString() throws Exception {

        final long firstCarId = 1;
        final String secondCarString = "{\"brand\":\"TestBrand\",\"model\":\"TestModel\"}";
        final MockMultipartFile file =
                new MockMultipartFile("file", "TestTitle.png",
                        MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        final Car car = Car.builder().id(1L).model("TestModel").brand("TestBrand").build();
        final String expected = CarDealershipController.REPLACE_CAR_SUCCESS_STRING;

        when(carRepository.findById(firstCarId)).thenReturn(Optional.of(car));
        when(converter.convert(secondCarString, Car.class)).thenReturn(car);

        final ResponseEntity<String> result = controller.replaceCar(1L, secondCarString, file);

        assertThat(result.getBody()).isEqualTo(expected);
    }

    @Test
    void givenInvalidReplacementRequest_whenValidatingCarToReplace_thenReturnErrorMessageString() {
        final long firstCarId = 1;
        final String secondCarString = "{\"brand\":\"TestBrand\",\"model\":\"TestModel\"}";
        final MockMultipartFile file =
                new MockMultipartFile("file", "TestTitle.png",
                        MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        final String expected = CarDealershipController.CAR_NOT_FOUND_STRING.formatted(1L);

        when(carRepository.findById(firstCarId)).thenReturn(Optional.empty());

        assertThrows(CarNotFoundException.class, () -> controller.replaceCar(firstCarId, secondCarString, file), expected);
    }

    @Test
    void givenValidRequestToDeleteCarWithImage_whenDeletingCar_thenReturnSuccessMessageString()
            throws Exception {

        final String expected = CarDealershipController.DELETE_CAR_SUCCESS_STRING;
        final Car car = Car.builder().id(1L).model("TestModel").brand("TestBrand").carImageList(List.of()).build();

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        final ResponseEntity<String> result = controller.deleteCar(1L);

        assertThat(result.hasBody()).isTrue();
        assertThat(result.getBody()).isEqualTo(expected);
    }

    @Test
    void givenInvalidRequestToDeleteCar_whenDeletingCar_thenReturnErrorMessageString() {
        final String expected = CarDealershipController.CAR_NOT_FOUND_STRING.formatted(1L);
        when(carRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CarNotFoundException.class, () -> controller.deleteCar(1L), expected);
    }

    @Test
    void givenValidDeleteCarsByBrandRequest_whenDeletingCars_thenReturnCarList() throws Exception {
        final String expected = CarDealershipController.DELETE_CAR_BY_BRAND_SUCCESS_STRING.formatted(2, "VW");
        final Car firstCar = Car.builder().brand("VW").carImageList(List.of()).build();
        final Car secondCar = Car.builder().brand("VW").carImageList(List.of()).build();
        when(carRepository.deleteCarByBrand("VW")).thenReturn(List.of(firstCar, secondCar));

        final ResponseEntity<String> result = controller.deleteCarByBrand("VW");

        assertThat(result.hasBody()).isTrue();
        assertThat(result.getBody()).isEqualTo(expected);
    }

}