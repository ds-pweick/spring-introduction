package de.doubleslash.spring.introduction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.model.CarImage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static de.doubleslash.spring.introduction.controller.CarDealershipService.*;

@RestController
@AllArgsConstructor
@Slf4j
public class CarDealershipController {

    private final CarDealershipService carDealershipService;

    @GetMapping(CARS_ROOT)
    public ResponseEntity<List<Car>> allCars() {
        return new ResponseEntity<>(carDealershipService.getAllCars(), HttpStatus.OK);
    }

    @GetMapping(IMAGES_ROOT)
    public ResponseEntity<List<CarImage>> allCarImages() {
        return new ResponseEntity<>(carDealershipService.getAllCarImages(), HttpStatus.OK);
    }

    @GetMapping(CARS_ROOT + "/{id}")
    public ResponseEntity<Car> get(@Valid @NotNull @PathVariable Long id) throws CarNotFoundException {
        return new ResponseEntity<>(carDealershipService.getCarIfValid(id), HttpStatus.OK);
    }

    @GetMapping(value = IMAGES_ROOT + "/{imageObjectName}", produces = {MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE, "image/webp"})
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@Valid @NotNull @PathVariable String imageObjectName) throws
            Exception {

        Pair<byte[], MediaType> imageData = carDealershipService.getImageIfValid(imageObjectName);

        return ResponseEntity.ok().contentType(imageData.getSecond()).contentLength(imageData.getFirst().length)
                .body(imageData.getFirst());
    }

    @PostMapping(value = CARS_ROOT + "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> addCarAndImage(@Valid @NotNull @RequestParam("car") String newCarJson,
                                                 @Valid @NotNull @RequestParam("file") MultipartFile imageOfNewCar)
            throws InvalidFileRequestException, CarModelAndOrBrandStringInvalidException, JsonProcessingException {

        Pair<Boolean, Car> carDataIfAdded = carDealershipService.addCarAndImageIfValid(newCarJson, imageOfNewCar);

        if (!carDataIfAdded.getFirst()) {
            return new ResponseEntity<>(FILE_UPLOAD_INTERNAL_ERROR_FAILURE_STRING, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return new ResponseEntity<>(carDataIfAdded.getSecond().toString(), HttpStatus.OK);
        }
    }

    @PostMapping(CARS_ROOT + "/replace")
    public ResponseEntity<String> replaceCar(@Valid @NotNull @RequestParam("oldId") Long oldCarId,
                                             @Valid @NotNull @RequestParam("car") String carString,
                                             @Valid @NotNull @RequestParam("file") MultipartFile file)
            throws Exception {

        Pair<Boolean, Car> carDataIfReplaced = carDealershipService.replaceCarIfValid(oldCarId, carString, file);

        if (!carDataIfReplaced.getFirst()) {
            return new ResponseEntity<>(FILE_UPLOAD_INTERNAL_ERROR_FAILURE_STRING, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return new ResponseEntity<>(carDataIfReplaced.getSecond().toString(), HttpStatus.OK);
        }
    }

    @DeleteMapping(CARS_ROOT + "/{id}")
    public ResponseEntity<String> deleteCar(@Valid @NotNull @PathVariable Long id) throws Exception {
        carDealershipService.deleteCarAndImageIfValid(id);

        return new ResponseEntity<>(DELETE_CAR_SUCCESS_STRING, HttpStatus.OK);
    }

    @DeleteMapping(CARS_ROOT + "/brand/{brand}")
    @Transactional
    public ResponseEntity<String> deleteCarByBrand(@Valid @NotNull @PathVariable String brand) throws Exception {
        String responseText = carDealershipService.deleteCarByBrand(brand);

        return new ResponseEntity<>(responseText, HttpStatus.OK);
    }

    enum AllowedExtension {
        png, jpg, jpeg, webp
    }
}