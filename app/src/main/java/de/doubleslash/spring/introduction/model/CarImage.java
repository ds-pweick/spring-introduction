package de.doubleslash.spring.introduction.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@AllArgsConstructor
@NoArgsConstructor(force = true)
@Data
@Entity
@Builder
@Slf4j
@JsonIncludeProperties(value = "imageObjectName")
public class CarImage {
    @ManyToOne
    @JsonBackReference
    private final Car associatedCar;
    @Id
    @GeneratedValue
    private Long id;
    private String imageObjectName;
    @Transient
    private MinIoFileHandler fileHandler;
    @Transient
    private String minIoBucketName;

    public CarImage(Car associatedCar, String imageObjectName, MinIoFileHandler fileHandler, String minIoBucketName) {
        this.associatedCar = associatedCar;
        this.imageObjectName = imageObjectName;
        this.fileHandler = fileHandler;
        this.minIoBucketName = minIoBucketName;
    }

    /*@PostRemove
    public void deleteImageObject() {
        log.info("CALLED");
        try {
            fileHandler.deleteFile(minIoBucketName, imageObjectName);
            log.error("Deleted file %s".formatted(imageObjectName));
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("File %s".formatted(imageObjectName), "could not be deleted due to exception", e);
        }
    }*/
}