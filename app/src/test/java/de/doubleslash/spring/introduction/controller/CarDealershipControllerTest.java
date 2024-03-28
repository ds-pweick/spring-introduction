package de.doubleslash.spring.introduction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.CarImage;
import de.doubleslash.spring.introduction.model.JsonStringToInstanceConverter;
import de.doubleslash.spring.introduction.model.MinioFileHandler;
import de.doubleslash.spring.introduction.repository.CarImageRepository;
import de.doubleslash.spring.introduction.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    @Mock
    private JsonStringToInstanceConverter converter;
    @Mock
    private MinioFileHandler minioFileHandler;
    private CarDealershipService carDealershipService;

    private CarDealershipController controller;

    @BeforeEach
    void setUp() {
        carDealershipService = new CarDealershipService(carRepository, carImageRepository, minioFileHandler, converter);
        controller = new CarDealershipController(carDealershipService);
    }

    @Test
    void givenRequestToGetAllCars_whenProcessingRequest_thenReturnAllCars() {
        final Car car = Car.builder().id(1L).model("TestModel").brand("TestBrand").build();
        List<Car> carList = List.of(car);

        when(carDealershipService.getAllCars()).thenReturn(carList);

        assertThat(controller.allCars().getBody()).isEqualTo(carList);
    }

    @Test
    void givenRequestToGetAllCarImages_whenProcessingRequest_thenReturnAllCarImages() {
        final Car car = Car.builder().id(1L).model("TestModel").brand("TestBrand").build();
        final CarImage carImage = CarImage.builder().associatedCar(car).imageObjectName("TestTitle.png").build();
        List<CarImage> carImageList = List.of(carImage);

        when(carDealershipService.getAllCarImages()).thenReturn(carImageList);

        assertThat(controller.allCarImages().getBody()).isEqualTo(carImageList);
    }

    @Test
    void givenRequestToAddCarAndImage_whenProcessingRequest_thenReturnSuccessMessage()
            throws CarModelAndOrBrandStringInvalidException, InvalidFileRequestException, JsonProcessingException {

        final String newCarJson = "{\"brand\":\"TestBrand\",\"model\":\"TestModel\"}";
        final Car car = Car.builder().id(1L).brand("TestBrand").model("TestModel").build();
        final MockMultipartFile file = new MockMultipartFile("TestTitle.png", "TestTitle.png",
                MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        final String expected = car.toString();

        when(converter.convert(newCarJson, Car.class)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);

        final ResponseEntity<String> result = controller.addCarAndImage(newCarJson, file);

        assertThat(result.getBody()).isEqualTo(expected);
    }

    @Test
    void givenInvalidRequestToAddCar_whenValidatingCarBrand_thenReturnErrorMessageString()
            throws JsonProcessingException {
        final String newCarJson = "{\"brand\":\"" + "TestBrand".repeat(500) + "\",\"model\":\"TestModel\"}";
        final Car car = Car.builder().brand("TestBrand".repeat(500)).model("TestModel").build();
        final MockMultipartFile file = new MockMultipartFile("file", "TestTitle.png",
                MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        when(converter.convert(newCarJson, Car.class)).thenReturn(car);
        assertThrows(CarModelAndOrBrandStringInvalidException.class, () -> controller.addCarAndImage(newCarJson, file));
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
        final Car car = Car.builder().id(1L).model("TestModel").brand("TestBrand").build();

        final String expected = CarDealershipService.FILE_UPLOAD_INVALID_NAME_FAILURE_STRING;

        when(converter.convert(carString, Car.class)).thenReturn(car);

        assertThrows(InvalidFileRequestException.class, () -> controller.addCarAndImage(carString, firstFile), expected);
        assertThrows(InvalidFileRequestException.class, () -> controller.addCarAndImage(carString, secondFile), expected);
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
    void givenCarId_whenGetCarById_thenReturnCar() throws CarNotFoundException {
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
        final String secondCarString = "{\"brand\":\"TestBrand1\",\"model\":\"TestModel\"}";
        final MockMultipartFile file =
                new MockMultipartFile("file", "TestTitle.png",
                        MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        final Car car = Car.builder().id(1L).model("TestModel").brand("TestBrand").build();

        when(carRepository.findById(firstCarId)).thenReturn(Optional.of(car));
        when(converter.convert(secondCarString, Car.class)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);

        final ResponseEntity<String> result = controller.replaceCar(1L, secondCarString, file);

        assertThat(result.getBody()).isEqualTo(car.toString());
    }

    @Test
    void givenInvalidReplacementRequest_whenValidatingCarToReplace_thenReturnErrorMessageString() {
        final long firstCarId = 1;
        final String secondCarString = "{\"brand\":\"TestBrand\",\"model\":\"TestModel\"}";
        final MockMultipartFile file =
                new MockMultipartFile("file", "TestTitle.png",
                        MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        final String expected = CarDealershipService.CAR_NOT_FOUND_STRING;

        when(carRepository.findById(firstCarId)).thenReturn(Optional.empty());

        assertThrows(CarNotFoundException.class, () -> controller.replaceCar(firstCarId, secondCarString, file), expected);
    }

    @Test
    void givenValidRequestToDeleteCarWithImage_whenDeletingCar_thenReturnSuccessMessageString()
            throws Exception {

        final String expected = CarDealershipService.DELETE_CAR_SUCCESS_STRING;
        final Car car = Car.builder().id(1L).model("TestModel").brand("TestBrand").carImageList(List.of()).build();

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        final ResponseEntity<String> result = controller.deleteCar(1L);

        assertThat(result.hasBody()).isTrue();
        assertThat(result.getBody()).isEqualTo(expected);
    }

    @Test
    void givenInvalidRequestToDeleteCar_whenDeletingCar_thenReturnErrorMessageString() {
        final String expected = CarDealershipService.CAR_NOT_FOUND_STRING;
        when(carRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CarNotFoundException.class, () -> controller.deleteCar(1L), expected);
    }

    @Test
    void givenValidDeleteCarsByBrandRequest_whenDeletingCars_thenReturnCarList() throws Exception {
        final String expected = CarDealershipService.DELETE_CAR_BY_BRAND_SUCCESS_STRING.formatted(2, "VW");
        final Car firstCar = Car.builder().brand("VW").carImageList(List.of()).build();
        final Car secondCar = Car.builder().brand("VW").carImageList(List.of()).build();
        when(carRepository.deleteCarByBrand("VW")).thenReturn(List.of(firstCar, secondCar));

        final ResponseEntity<String> result = controller.deleteCarByBrand("VW");

        assertThat(result.hasBody()).isTrue();
        assertThat(result.getBody()).isEqualTo(expected);
    }

}