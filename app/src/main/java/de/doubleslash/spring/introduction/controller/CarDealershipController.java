package de.doubleslash.spring.introduction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.JsonStringToInstance;
import de.doubleslash.spring.introduction.model.MinIoFileHandler;
import de.doubleslash.spring.introduction.repository.CarRepository;
import io.minio.errors.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@Slf4j
public class CarDealershipController {
    public final static String ADD_CAR_SUCCESS_STRING = "Successfully added car %s %s";

    public final static String CAR_JSON_PARSE_FAILURE_STRING = "Car data sent is no valid JSON";
    public final static String MODEL_OR_BRAND_INVALID_STRING = "Car model and/or brand name invalid";
    public final static String FILE_UPLOAD_INVALID_NAME_FAILURE_STRING = "Name of file requested for upload is invalid," +
            " too long, or contains an extension which is prohibited.";
    public final static String FILE_UPLOAD_INTERNAL_ERROR_FAILURE_STRING = "File upload failed due to internal error.";
    public final static String CARS_BUCKET = "car-images";
    private final static String CARS_ROOT = "/cars";
    private final static String CARS_ROOT_WITH_IMAGE = CARS_ROOT + "/with-image";
    public static String CAR_NOT_FOUND_STRING = "No car with requested id %d found";
    public static String REPLACE_CAR_SUCCESS_STRING = "Replacement successful";
    public static String DELETE_CAR_SUCCESS_STRING = "Deletion successful";
    public static String DELETE_CAR_BY_BRAND_SUCCESS_STRING = "Successfully deleted %d car(s) of brand %s";
    public static String DELETE_CAR_BY_BRAND_NONE_DELETED_NEUTRAL_STRING = "No cars were deleted";

    private final CarRepository repository;

    private final JsonStringToInstance converter;

    private final MinIoFileHandler fileHandler;

    @GetMapping(CARS_ROOT)
    public ResponseEntity<List<Car>> allCars() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    @GetMapping(CARS_ROOT + "/{id}")
    public ResponseEntity<Car> get(@Valid @NotNull @PathVariable long id) throws CarNotFoundException {
        Optional<Car> optionalCar = repository.findById(id);
        if (optionalCar.isEmpty()) {
            throw new CarNotFoundException(CAR_NOT_FOUND_STRING.formatted(id));
        }

        return new ResponseEntity<>(optionalCar.get(), HttpStatus.OK);
    }

    @GetMapping(value = CARS_ROOT + "/{id}/with-image", produces = {MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE, "image/webp"
    })
    @ResponseBody
    public ResponseEntity<ByteArrayResource> getImageForCar(@Valid @NotNull @PathVariable long id) throws
            CarNotFoundException, MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {

        Optional<Car> optionalCar = repository.findById(id);
        if (optionalCar.isEmpty()) {
            throw new CarNotFoundException(CAR_NOT_FOUND_STRING.formatted(id));
        }

        ByteArrayResource resource = fileHandler.downloadFile(CARS_BUCKET, optionalCar.get().getImageObjectName());

        AllowedExtension fileExtension = AllowedExtension.valueOf(optionalCar.get().getImageObjectName()
                .split("\\.")[1]);

        MediaType mediaType = switch (fileExtension) {
            case png -> MediaType.IMAGE_PNG;
            case jpg, jpeg -> MediaType.IMAGE_JPEG;
            case webp -> MediaType.valueOf("image/webp");
        };

        return ResponseEntity.ok().contentType(mediaType).contentLength(resource.contentLength()).body(resource);
    }

    @PostMapping(value = CARS_ROOT_WITH_IMAGE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> addCar(@Valid @NotNull @RequestParam("car") String carString, @Valid @NotNull @RequestParam("file") MultipartFile file)
            throws InvalidFileRequestException, CarModelOrBrandStringInvalidException {

        // car is in json, need to use converter because of multipart content type
        Car car;
        try {
            car = converter.convert(carString, Car.class);
            if (car == null || !validateCarBrandAndModelStringLengths(car)) {
                throw new CarModelOrBrandStringInvalidException(MODEL_OR_BRAND_INVALID_STRING);
            }
        } catch (JsonProcessingException e) {
            log.error("Endpoint %s received invalid input for the car's JSON".formatted(CARS_ROOT_WITH_IMAGE), e);
            return new ResponseEntity<>(CAR_JSON_PARSE_FAILURE_STRING, HttpStatus.BAD_REQUEST);
        }

        return uploadIfValidated(file, car, ADD_CAR_SUCCESS_STRING.formatted(car.getBrand(), car.getModel()));
    }

    @PostMapping(CARS_ROOT + "/replace")
    public ResponseEntity<String> replaceCar(@Valid @NotNull @RequestParam("firstCarId") Long firstCarId,
                                             @Valid @NotNull @RequestParam("secondCar") String carString,
                                             @Valid @NotNull @RequestParam("secondCarFile") MultipartFile file) throws Exception {

        Optional<Car> optionalCar = repository.findById(firstCarId);

        Car secondCar;
        try {
            secondCar = converter.convert(carString, Car.class);
        } catch (JsonProcessingException e) {
            log.error("Endpoint %s received invalid input for the second car's JSON"
                    .formatted(CARS_ROOT + "/replace"), e);
            return new ResponseEntity<>(CAR_JSON_PARSE_FAILURE_STRING, HttpStatus.BAD_REQUEST);
        }

        if (optionalCar.isEmpty()) {
            throw new CarNotFoundException(CAR_NOT_FOUND_STRING.formatted(firstCarId));
        } else if (!validateCarBrandAndModelStringLengths(secondCar)) {
            throw new CarModelOrBrandStringInvalidException(MODEL_OR_BRAND_INVALID_STRING);
        }

        repository.deleteById(firstCarId);
        fileHandler.deleteFile(CARS_BUCKET, optionalCar.get().getImageObjectName());

        return uploadIfValidated(file, secondCar, REPLACE_CAR_SUCCESS_STRING);
    }

    private ResponseEntity<String> uploadIfValidated(MultipartFile file, Car car, String responseText) throws InvalidFileRequestException {
        Pair<Boolean, String> fileValidationResult = validateUploadedImageFile(file);

        if (!fileValidationResult.getFirst()) {
            throw new InvalidFileRequestException(FILE_UPLOAD_INVALID_NAME_FAILURE_STRING);
        }

        try (InputStream inputStream = file.getInputStream()) {
            String savedFilename = fileHandler.uploadFile(CARS_BUCKET, inputStream,
                    file.getSize(), fileValidationResult.getSecond());

            // now that image object name is known, set property and save
            car.setImageObjectName(savedFilename);
            repository.save(car);
        } catch (IOException | ServerException | InsufficientDataException | ErrorResponseException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            log.error("Requested multipart data upload failed due to exception: %s".formatted(e.getMessage()));
            return new ResponseEntity<>(FILE_UPLOAD_INTERNAL_ERROR_FAILURE_STRING, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(responseText, HttpStatus.OK);
    }

    @DeleteMapping(CARS_ROOT + "/{id}")
    public ResponseEntity<String> deleteCar(@Valid @NotNull @PathVariable Long id) throws CarNotFoundException,
            MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        Optional<Car> optionalCar = repository.findById(id);
        if (optionalCar.isEmpty()) {
            throw new CarNotFoundException(CAR_NOT_FOUND_STRING.formatted(id));
        }

        String associatedImageObjectName = optionalCar.get().getImageObjectName();

        repository.deleteById(id);
        fileHandler.deleteFile(CARS_BUCKET, associatedImageObjectName);

        return new ResponseEntity<>(DELETE_CAR_SUCCESS_STRING, HttpStatus.OK);
    }

    @DeleteMapping(CARS_ROOT + "/brand/{brand}")
    @Transactional
    public ResponseEntity<String> deleteCarByBrand(@Valid @NotNull @PathVariable String brand) throws MinioException,
            IOException, NoSuchAlgorithmException, InvalidKeyException {
        String responseText;
        List<Car> deleted = repository.deleteCarByBrand(brand);

        if (!deleted.isEmpty()) {
            fileHandler.deleteMultiple(CARS_BUCKET, deleted.stream().map(Car::getImageObjectName).toList());
            responseText = DELETE_CAR_BY_BRAND_SUCCESS_STRING.formatted(deleted.size(), brand);
        } else {
            responseText = DELETE_CAR_BY_BRAND_NONE_DELETED_NEUTRAL_STRING;
        }

        return new ResponseEntity<>(responseText, HttpStatus.OK);
    }

    /**
     * Validates name of uploaded image file. Returns the validation result and, if result is <code>True</code>,
     * the extension of the valid file. Otherwise, the second member of the <code>Pair</code> will be an empty String.
     */
    private Pair<Boolean, String> validateUploadedImageFile(MultipartFile file) {

        // get filename as present on client system
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isEmpty() || originalFilename.length() >= 255) {
            return Pair.of(false, "");
        }

        String[] substrings = originalFilename.split("\\.");
        // just for easier access of last element
        String fileExtension = substrings[1];

        // make sure that there is no trickery like file.php.jpg or sth like that
        // and that file has no extension other than jpg, jpeg or png
        if (substrings.length != 2 || !isValidExtension(fileExtension)) return Pair.of(false, "");

        return Pair.of(true, fileExtension);
    }

    private Boolean validateCarBrandAndModelStringLengths(Car car) {
        int modelStringLength = car.getModel().length();
        int brandStringLength = car.getBrand().length();
        return modelStringLength > 0 && modelStringLength <= 300 && brandStringLength > 0 && brandStringLength <= 100;
    }

    private Boolean isValidExtension(String fileExtension) {
        return Arrays.stream(AllowedExtension.values()).anyMatch(allowedExtension -> allowedExtension.name()
                .equals(fileExtension));
    }

    private enum AllowedExtension {
        png, jpg, jpeg, webp
    }
}