package de.doubleslash.spring.introduction.controller;

import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.CarCheckMappingRequest;
import de.doubleslash.spring.introduction.model.MinIoFileHandler;
import de.doubleslash.spring.introduction.repository.CarRepository;
import io.minio.errors.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final CarRepository repository;
    private final List<String> allowedImageExtensions = List.of("jpg", "jpeg", "png");

    @GetMapping("/cars")
    public ResponseEntity<List<Car>> all() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/cars/{id}")
    public ResponseEntity<String> get(@Valid @NotNull @PathVariable long id) throws CarNotFoundException {
        Optional<Car> optionalCar = repository.findById(id);
        if (optionalCar.isEmpty()) {
            throw new CarNotFoundException(getErrorMessageForId(id));
        }

        return new ResponseEntity<>(optionalCar.get().toString(), HttpStatus.OK);
    }

    @PostMapping("/cars/add")
    public ResponseEntity<String> addCar(@Valid @NotNull @RequestBody Car car) throws
            CarModelOrBrandStringTooLongException {
        if (!validateCarBrandAndModelStringLengths(car)) {
            throw new CarModelOrBrandStringTooLongException("Car model and/or brand name exceeds the character limit");
        }
        repository.save(car);

        return new ResponseEntity<>("Successfully added car %s %s".formatted(car.getBrand(), car.getModel()), HttpStatus.OK);
    }

    @PostMapping(value = "/cars/add/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> addCarImage(@NotNull @RequestParam("file") MultipartFile file) throws IOException,
            ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException,
            InvalidFileUploadException {

        // get filename as present on client system
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.length() >= 255) {
            throw new InvalidFileUploadException("Requested file upload has invalid name");
        }

        String[] substrings = originalFilename.split("\\.");
        // just for easier access of last element
        String fileExtension = Arrays.stream(substrings).toList().get(1);

        // make sure that there is no trickery like file.php.jpg or sth like that
        // and that file has no extension other than jpg, jpeg or png
        if (substrings.length != 2 || !allowedImageExtensions.contains(fileExtension)) {
            throw new InvalidFileUploadException("Requested file upload has invalid or prohibited file extension");
        }

        MinIoFileHandler.uploadFile("cars", file.getInputStream(),
                file.getSize(), fileExtension);

        return new ResponseEntity<>("Successfully uploaded image", HttpStatus.OK);
    }

    @PostMapping("/cars/replace")
    public ResponseEntity<String> replaceCar(@Valid @RequestBody CarCheckMappingRequest mappingRequest) throws
            CarNotFoundException, CarModelOrBrandStringTooLongException {
        Car firstCar = mappingRequest.firstCar;
        Car secondCar = mappingRequest.secondCar;

        if (!validateCarBrandAndModelStringLengths(secondCar)) {
            throw new CarModelOrBrandStringTooLongException("Car model and/or brand name exceeds the character limit");
        }

        if (firstCar.getId() == null || !repository.existsById(firstCar.getId())) {
            throw new CarNotFoundException(getErrorMessageForId(firstCar.getId()));
        }
        repository.deleteById(firstCar.getId());
        repository.save(secondCar);

        return new ResponseEntity<>("Replacement successful", HttpStatus.OK);
    }

    @DeleteMapping("/cars/{id}")
    public ResponseEntity<String> deleteCar(@Valid @NotNull @PathVariable Long id) throws CarNotFoundException {
        if (!repository.existsById(id)) {
            throw new CarNotFoundException();
        }
        repository.deleteById(id);

        return new ResponseEntity<>("Deletion successful", HttpStatus.OK);
    }

    @DeleteMapping("/cars/brand/{brand}")
    @Transactional
    public ResponseEntity<String> deleteCarByBrand(@Valid @NotNull @PathVariable String brand) {
        String responseText;
        List<Car> deleted = repository.deleteCarByBrand(brand);
        if (!deleted.isEmpty()) {
            responseText = "Successfully deleted %d car(s) of brand %s".formatted(deleted.size(), brand);
        } else {
            responseText = "No cars were deleted";
        }

        return new ResponseEntity<>(responseText, HttpStatus.OK);
    }

    private String getErrorMessageForId(Long id) {
        return "No car with requested id %d found".formatted(id);
    }

    private Boolean validateCarBrandAndModelStringLengths(Car car) {
        return car.getModel().length() <= 300 && car.getBrand().length() <= 100;
    }


}