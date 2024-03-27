package de.doubleslash.spring.introduction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.CarImage;
import de.doubleslash.spring.introduction.model.JsonStringToInstanceConverter;
import de.doubleslash.spring.introduction.model.MinioFileHandler;
import de.doubleslash.spring.introduction.repository.CarImageRepository;
import de.doubleslash.spring.introduction.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CarDealershipServiceTest {
    @Mock
    private CarRepository carRepository;
    @Mock
    private CarImageRepository carImageRepository;
    @Mock
    private MinioFileHandler minioFileHandler;
    @Mock
    private JsonStringToInstanceConverter converter;

    @InjectMocks
    private CarDealershipService service;

    @Test
    void givenImageFilename_whenChoosingMediaType_thenReturnCorrectType() {
        assertThat(CarDealershipService.getMediaType("png")).isEqualTo(MediaType.IMAGE_PNG);
        assertThat(CarDealershipService.getMediaType("jpg")).isEqualTo(MediaType.IMAGE_JPEG);
        assertThat(CarDealershipService.getMediaType("webp")).isEqualTo(MediaType.valueOf("image/webp"));
    }

    @Test
    void givenValidCar_whenCheckingCarExistence_thenReturnTrue() {
        final Car car = Car.builder().id(1L).build();
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        assertThat(service.carExists(1L)).isTrue();
    }

    @Test
    void givenInvalidCar_whenCheckingCarExistence_thenReturnFalse() {
        when(carRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(service.carExists(1L)).isFalse();
    }

    @Test
    void givenValidCar_whenFetchingCar_thenReturnCar() throws CarNotFoundException {
        final Car car = Car.builder().id(1L).build();
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        assertThat(service.getCarIfValid(1L)).isEqualTo(car);
    }

    @Test
    void givenInvalidCar_whenFetchingCar_thenThrowException() {
        final Car car = Car.builder().id(1L).build();
        when(carRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CarNotFoundException.class, () -> assertThat(service.getCarIfValid(1L)).isEqualTo(car));
    }

    @Test
    void givenValidImage_whenFetchingImage_thenReturnImageData() throws Exception {
        String imageObjectName = "TestTitle.png";
        byte[] bytes = new byte[1];

        when(minioFileHandler.downloadFile(imageObjectName, CarDealershipService.CARS_BUCKET))
                .thenReturn(bytes);

        assertThat(service.getImageIfValid(imageObjectName)).isEqualTo(Pair.of(bytes, MediaType.IMAGE_PNG));
    }

    @Test
    void givenInvalidRequestToAddCarAndImage_whenAddingCarAndImage_thenThrowException() {
        Car newCar = Car.builder().brand("TestBrand".repeat(500)).model("TestModel").build();

        assertThat(service.validateCarBrandAndModelStringLengths(newCar)).isFalse();
    }

    @Test
    void givenValidImageFilename_whenValidatingImageFilename_thenReturnTrueAndExtension() {
        String filename = "TestTitle.png";
        assertThat(service.validateImageFilenameAndReturnExtension(filename)).isEqualTo(Pair.of(true, "png"));
    }

    @Test
    void givenInvalidImageFilename_whenValidatingImageFilename_thenReturnFalseAndEmptyString() {
        String filename = "TestTitle.exe.png";
        assertThat(service.validateImageFilenameAndReturnExtension(filename)).isEqualTo(Pair.of(false, ""));
    }

    @Test
    void givenValidImageFileExtension_whenValidatingExtension_thenReturnTrue() {
        String extension = "png";

        assertThat(service.isValidExtension(extension)).isTrue();
    }

    @Test
    void givenInvalidImageFileExtension_whenValidatingExtension_thenReturnFalse() {
        String extension = "exe";

        assertThat(service.isValidExtension(extension)).isFalse();
    }

    @Test
    void givenValidRequestToAddCarAndImage_whenAddingCarAndImage_thenReturnTrue()
            throws InvalidFileRequestException, CarModelAndOrBrandStringInvalidException, JsonProcessingException {
        final String newCarJson = "{\"brand\":\"TestBrand\",\"model\":\"TestModel\"}";
        final Car car = Car.builder().brand("TestBrand").model("TestModel").build();
        final MockMultipartFile file = new MockMultipartFile("TestTitle.png", "TestTitle.png",
                MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        when(converter.convert(newCarJson, Car.class)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);

        assertThat(service.addCarAndImageIfValid(newCarJson, file)).isEqualTo(Pair.of(true, car));
    }

    @Test
    void givenValidRequestToReplaceCar_whenReplacingCarAndUploadingNewCarAndImage_thenReturnTrue()
            throws JsonProcessingException, CarModelAndOrBrandStringInvalidException, InvalidFileRequestException,
            CarNotFoundException {

        final String newCarJson = "{\"brand\":\"TestBrand\",\"model\":\"TestModel\"}";
        final Car car = Car.builder().id(1L).build();
        final Car newCar = Car.builder().id(2L).brand("TestBrand").model("TestModel").build();
        final MockMultipartFile imageOfNewCar = new MockMultipartFile("TestTitle.png", "TestTitle.png",
                MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(converter.convert(newCarJson, Car.class)).thenReturn(newCar);
        when(carRepository.save(newCar)).thenReturn(newCar);

        assertThat(service.replaceCarIfValid(1L, newCarJson, imageOfNewCar)).isEqualTo(Pair.of(true, newCar));
    }

    @Test
    void givenValidRequestToDeleteCar_whenCleaningUpImages_thenUseCorrectListOfImageObjectNames() throws Exception {
        final List<String> imageObjectNames = List.of("Test1.png", "Test2.png", "Test3.jpg", "Test4.webp");
        final Car car = Car.builder().id(1L).build();
        final List<CarImage> carImages =
                imageObjectNames.stream().map(imageObjectName ->
                        CarImage.builder().associatedCar(car).imageObjectName(imageObjectName).build()).toList();

        car.setCarImageList(carImages);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        service.deleteCarAndImageIfValid(1L);

        verify(minioFileHandler).deleteMultiple(imageObjectNames, CarDealershipService.CARS_BUCKET);
    }

    @Test
    void givenValidRequestToDeleteCarsByBrand_whenDeletingCars_thenReturnAccurateString() throws Exception {
        List<String> imageObjectNames = new ArrayList<>();
        List<Car> carList = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            imageObjectNames.add("Test%d.png".formatted(i));
        }

        // for each car, get four images, e.g. Test0 to Test3, build carImage entity list from these
        for (int i = 0; i < 5; i++) {
            List<String> imageObjectNamesForCurrentCar = imageObjectNames.subList(i * 4, i * 4 + 4);
            Car car = Car.builder().brand("TestBrand").build();

            List<CarImage> carImageEntities = imageObjectNamesForCurrentCar.stream().map(carImageName ->
                    CarImage.builder().associatedCar(car).imageObjectName(carImageName).build()).toList();

            car.setCarImageList(carImageEntities);
            carList.add(car);
        }

        when(carRepository.deleteCarByBrand("TestBrand")).thenReturn(carList);
        assertThat(service.deleteCarByBrand("TestBrand"))
                .isEqualTo(CarDealershipService.DELETE_CAR_BY_BRAND_SUCCESS_STRING.formatted(5, "TestBrand"));
    }

    @Test
    void givenInvalidRequestToDeleteCarsByBrand_whenDeletingCars_thenReturnAccurateString() throws Exception {
        when(carRepository.deleteCarByBrand("TestBrand")).thenReturn(List.of());
        assertThat(service.deleteCarByBrand("TestBrand"))
                .isEqualTo(CarDealershipService.DELETE_CAR_BY_BRAND_NONE_DELETED_NEUTRAL_STRING);
    }

}
