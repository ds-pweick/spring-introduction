package de.doubleslash.spring.introduction.controller;

import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.CarCheckMappingRequest;
import de.doubleslash.spring.introduction.model.JsonToCarConverter;
import de.doubleslash.spring.introduction.model.MinIoFileHandler;
import de.doubleslash.spring.introduction.repository.CarRepository;
import io.minio.errors.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public final static String MODEL_OR_BRAND_INVALID_STRING = "Car model and/or brand name invalid";
    public final static String FILE_UPLOAD_SUCCESS_STRING = "Successfully uploaded image";
    public final static String FILE_UPLOAD_FAILURE_STRING = "Name of file requested for upload is invalid, too long, or " +
            "contains an extension which is prohibited.";
    public static String CAR_NOT_FOUND_STRING = "No car with requested id %d found";
    public static String REPLACE_CAR_SUCCESS_STRING = "Replacement successful";
    public static String DELETE_CAR_SUCCESS_STRING = "Deletion successful";
    public static String DELETE_CAR_BY_BRAND_SUCCESS_STRING = "Successfully deleted %d car(s) of brand %s";
    public static String DELETE_CAR_BY_BRAND_NONE_DELETED_STRING = "No cars were deleted";

    private final CarRepository repository;
    private final List<String> allowedImageExtensions = List.of("jpg", "jpeg", "png", "webp");

    private final JsonToCarConverter converter;

    @GetMapping("/cars")
    public ResponseEntity<List<Car>> all() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/cars/{id}")
    public ResponseEntity<String> get(@Valid @NotNull @PathVariable long id) throws CarNotFoundException {
        Optional<Car> optionalCar = repository.findById(id);
        if (optionalCar.isEmpty()) {
            throw new CarNotFoundException(CAR_NOT_FOUND_STRING.formatted(id));
        }

        return new ResponseEntity<>(optionalCar.get().toString(), HttpStatus.OK);
    }

    @PostMapping("/cars/add")
    public ResponseEntity<String> addCar(@Valid @NotNull @RequestBody Car car) throws
            CarModelOrBrandStringInvalidException {
        if (!validateCarBrandAndModelStringLengths(car)) {
            throw new CarModelOrBrandStringInvalidException(MODEL_OR_BRAND_INVALID_STRING);
        }
        repository.save(car);

        return new ResponseEntity<>(ADD_CAR_SUCCESS_STRING.formatted(car.getBrand(), car.getModel()), HttpStatus.OK);
    }

    @PostMapping(value = "/cars/addWithImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> addCarWithImage(@Valid @NotNull @RequestParam("file") MultipartFile file,
                                                  @Valid @NotNull @RequestParam("car") String carString)
            throws IOException, ServerException, InsufficientDataException, ErrorResponseException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
            InternalException, InvalidFileUploadException, CarModelOrBrandStringInvalidException {

        Car car = converter.convert(carString);

        if (car==null || !validateCarBrandAndModelStringLengths(car)) {
            throw new CarModelOrBrandStringInvalidException(MODEL_OR_BRAND_INVALID_STRING);
        }
        long savedCarId = repository.save(car).getId();

        Pair<Boolean, String> fileValidationResult = validateUploadedImageFile(file);

        if (!fileValidationResult.getFirst()) {
            throw new InvalidFileUploadException(FILE_UPLOAD_FAILURE_STRING);
        }

        MinIoFileHandler.uploadFile("cars", file.getInputStream(),
                file.getSize(), fileValidationResult.getSecond(), savedCarId);

        return new ResponseEntity<>(FILE_UPLOAD_SUCCESS_STRING, HttpStatus.OK);
    }

    @PostMapping("/cars/replace")
    public ResponseEntity<String> replaceCar(@Valid @RequestBody CarCheckMappingRequest mappingRequest) throws
            CarNotFoundException, CarModelOrBrandStringInvalidException {
        Car firstCar = mappingRequest.firstCar;
        Car secondCar = mappingRequest.secondCar;

        if (!validateCarBrandAndModelStringLengths(secondCar)) {
            throw new CarModelOrBrandStringInvalidException(MODEL_OR_BRAND_INVALID_STRING);
        }

        if (firstCar.getId() == null || !repository.existsById(firstCar.getId())) {
            throw new CarNotFoundException(CAR_NOT_FOUND_STRING.formatted(firstCar.getId()));
        }
        repository.deleteById(firstCar.getId());
        repository.save(secondCar);

        return new ResponseEntity<>(REPLACE_CAR_SUCCESS_STRING, HttpStatus.OK);
    }

    @DeleteMapping("/cars/{id}")
    public ResponseEntity<String> deleteCar(@Valid @NotNull @PathVariable Long id) throws CarNotFoundException {
        if (!repository.existsById(id)) {
            throw new CarNotFoundException(CAR_NOT_FOUND_STRING.formatted(id));
        }
        repository.deleteById(id);

        return new ResponseEntity<>(DELETE_CAR_SUCCESS_STRING, HttpStatus.OK);
    }

    @DeleteMapping("/cars/brand/{brand}")
    @Transactional
    public ResponseEntity<String> deleteCarByBrand(@Valid @NotNull @PathVariable String brand) {
        String responseText;
        List<Car> deleted = repository.deleteCarByBrand(brand);
        if (!deleted.isEmpty()) {
            responseText = DELETE_CAR_BY_BRAND_SUCCESS_STRING.formatted(deleted.size(), brand);
        } else {
            responseText = DELETE_CAR_BY_BRAND_NONE_DELETED_STRING;
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

        if (originalFilename == null || originalFilename.length() >= 255) return Pair.of(false, "");

        String[] substrings = originalFilename.split("\\.");
        // just for easier access of last element
        String fileExtension = Arrays.stream(substrings).toList().get(1);

        // make sure that there is no trickery like file.php.jpg or sth like that
        // and that file has no extension other than jpg, jpeg or png
        if (substrings.length != 2 || !allowedImageExtensions.contains(fileExtension)) return Pair.of(false, "");

        return Pair.of(true, fileExtension);
    }

    private Boolean validateCarBrandAndModelStringLengths(Car car) {
        int modelStringLength = car.getModel().length();
        int brandStringLength = car.getBrand().length();
        return modelStringLength > 0 && modelStringLength <= 300 && brandStringLength > 0 && brandStringLength <= 100;
    }

}