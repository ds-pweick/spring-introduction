package de.doubleslash.spring.introduction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.doubleslash.spring.introduction.model.BlobStoreFileHandler;
import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.CarImage;
import de.doubleslash.spring.introduction.model.JsonStringToInstanceConverter;
import de.doubleslash.spring.introduction.repository.CarImageRepository;
import de.doubleslash.spring.introduction.repository.CarRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@AllArgsConstructor
public class CarDealershipService {
    public final static String ADD_CAR_AND_IMAGE_SUCCESS_STRING = "Successfully added car along with its image.";
    public final static String CAR_JSON_PARSE_FAILURE_STRING = "Car data sent is no valid JSON.";
    public final static String CAR_MODEL_AND_OR_BRAND_NAME_INVALID_STRING = "Car model and/or brand name invalid.";
    public final static String FILE_UPLOAD_INVALID_NAME_FAILURE_STRING = "Name of file requested for upload is invalid," +
            " too long, or contains an extension which is prohibited.";
    public final static String FILE_UPLOAD_INTERNAL_ERROR_FAILURE_STRING = "File upload failed due to internal error.";
    public final static String CARS_BUCKET = "car-images";
    public final static String CARS_ROOT = "/cars";
    public final static String IMAGES_ROOT = "/images";
    public final static String CAR_NOT_FOUND_STRING = "No car with requested id found.";
    public final static String REPLACE_CAR_SUCCESS_STRING = "Replacement successful.";
    public final static String DELETE_CAR_SUCCESS_STRING = "Deletion successful.";
    public final static String DELETE_CAR_BY_BRAND_SUCCESS_STRING = "Successfully deleted %d car(s) of brand %s.";
    public final static String DELETE_CAR_BY_BRAND_NONE_DELETED_NEUTRAL_STRING = "No cars were deleted.";

    private CarRepository carRepository;
    private CarImageRepository carImageRepository;
    private BlobStoreFileHandler fileHandler;
    private JsonStringToInstanceConverter converter;

    @NotNull
    public static MediaType getMediaType(String fileExtension) {
        return switch (AllowedExtension.valueOf(fileExtension)) {
            case png -> MediaType.IMAGE_PNG;
            case jpg, jpeg -> MediaType.IMAGE_JPEG;
            case webp -> MediaType.valueOf("image/webp");
        };
    }

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public List<CarImage> getAllCarImages() {
        return carImageRepository.findAll();
    }

    public Boolean carExists(Long id) {
        return carRepository.findById(id).isPresent();
    }

    public Car getCarIfValid(Long id) throws CarNotFoundException {
        Optional<Car> optionalCar = carRepository.findById(id);
        if (optionalCar.isEmpty()) {
            throw new CarNotFoundException(CAR_NOT_FOUND_STRING);
        }
        return optionalCar.get();
    }

    public Pair<byte[], MediaType> getImageIfValid(String imageObjectName) throws Exception {
        Pair<Boolean, String> fileValidationResult = validateImageFilenameAndReturnExtension(imageObjectName);

        if (!fileValidationResult.getFirst()) {
            throw new InvalidFileRequestException(FILE_UPLOAD_INVALID_NAME_FAILURE_STRING);
        }

        MediaType mediaType = getMediaType(fileValidationResult.getSecond());

        return Pair.of(fileHandler.downloadFile(imageObjectName, CARS_BUCKET), mediaType);
    }

    public Car carFromJsonIfValid(String carString) throws CarModelAndOrBrandStringInvalidException, JsonProcessingException {
        Car car = converter.convert(carString, Car.class);
        if (!validateCarBrandAndModelStringLengths(car)) {
            throw new CarModelAndOrBrandStringInvalidException(CAR_MODEL_AND_OR_BRAND_NAME_INVALID_STRING);
        }

        return car;
    }

    public Pair<Boolean, Car> addCarAndImageIfValid(String newCarJson, MultipartFile imageOfNewCar)
            throws CarModelAndOrBrandStringInvalidException, JsonProcessingException, InvalidFileRequestException {
        Car car = carFromJsonIfValid(newCarJson);
        return addCarAndUploadImageIfValidated(car, imageOfNewCar);
    }

    public Pair<Boolean, Car> replaceCarIfValid(Long oldCarId, String newCarJson, MultipartFile imageOfNewCar)
            throws CarNotFoundException, CarModelAndOrBrandStringInvalidException, InvalidFileRequestException, JsonProcessingException {
        if (!carExists(oldCarId)) {
            throw new CarNotFoundException(CAR_NOT_FOUND_STRING);
        }

        Car newCar = carFromJsonIfValid(newCarJson);

        carRepository.deleteById(oldCarId);
        return addCarAndUploadImageIfValidated(newCar, imageOfNewCar);
    }

    public void deleteCarAndImageIfValid(Long id) throws Exception {
        Car car = getCarIfValid(id);
        List<String> associatedImageObjects = car.getCarImageList().stream()
                .map(CarImage::getImageObjectName).toList();

        carRepository.deleteById(id);
        fileHandler.deleteMultiple(associatedImageObjects, CARS_BUCKET);
    }

    public String deleteCarByBrand(String brand) throws Exception {
        List<Car> deleted = carRepository.deleteCarByBrand(brand);

        if (!deleted.isEmpty()) {
            cleanUpImageFiles(deleted);
            return DELETE_CAR_BY_BRAND_SUCCESS_STRING.formatted(deleted.size(), brand);
        } else {
            return DELETE_CAR_BY_BRAND_NONE_DELETED_NEUTRAL_STRING;
        }
    }

    public void cleanUpImageFiles(List<Car> deleted) throws Exception {
        List<List<String>> imageObjectListList = deleted.stream()
                .map(Car::getCarImageList)
                .map(carImageList -> carImageList.stream()
                        .map(CarImage::getImageObjectName).toList())
                .toList();
        for (List<String> imageObjectList : imageObjectListList) {
            fileHandler.deleteMultiple(imageObjectList, CARS_BUCKET);
        }
    }

    private Pair<Boolean, Car> addCarAndUploadImageIfValidated(Car car, MultipartFile imageOfNewCar)
            throws InvalidFileRequestException {

        boolean carAndImageUploaded = false;

        Pair<Boolean, String> fileValidationResult = validateImageFilenameAndReturnExtension(imageOfNewCar
                .getOriginalFilename());

        if (!fileValidationResult.getFirst()) {
            throw new InvalidFileRequestException(FILE_UPLOAD_INVALID_NAME_FAILURE_STRING);
        }

        try (InputStream inputStream = imageOfNewCar.getInputStream()) {
            String savedFilename = fileHandler.uploadFile(inputStream,
                    imageOfNewCar.getSize(), fileValidationResult.getSecond(), CARS_BUCKET);
            // now that image object name is known, save new entity
            CarImage carImage = new CarImage(car, savedFilename);

            carRepository.save(car);
            carImageRepository.save(carImage);
            carAndImageUploaded = true;
        } catch (Exception e) {
            log.error("Requested multipart data upload failed due to exception: %s".formatted(e.getMessage()));
        }

        return Pair.of(carAndImageUploaded, car);
    }

    /**
     * Validates name of uploaded image file. Returns the validation result and, if result is <code>True</code>,
     * the extension of the valid file. Otherwise, the second member of the <code>Pair</code> will be an empty String.
     */
    public Pair<Boolean, String> validateImageFilenameAndReturnExtension(String filename) {

        if (filename == null || filename.isEmpty() || filename.length() >= 255) {
            return Pair.of(false, "");
        }

        String[] substrings = filename.split("\\.");
        // just for easier access of last element
        String fileExtension = substrings[1];

        // make sure that there is no trickery like file.php.jpg or sth like that
        // and that file has no extension other than jpg, jpeg or png
        if (substrings.length != 2 || !isValidExtension(fileExtension)) return Pair.of(false, "");

        return Pair.of(true, fileExtension);
    }

    public Boolean validateCarBrandAndModelStringLengths(Car car) {
        int modelStringLength = car.getModel().length();
        int brandStringLength = car.getBrand().length();
        return modelStringLength != 0 && modelStringLength <= 300 && brandStringLength != 0 && brandStringLength <= 100;
    }

    public Boolean isValidExtension(String fileExtension) {
        return Arrays.stream(CarDealershipController.AllowedExtension.values()).anyMatch(allowedExtension -> allowedExtension.name()
                .equals(fileExtension));
    }

    private enum AllowedExtension {
        png, jpg, jpeg, webp
    }
}
