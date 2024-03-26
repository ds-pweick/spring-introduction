package de.doubleslash.spring.introduction.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor(force = true)
@Data
@Entity
@Builder
@JsonIncludeProperties(value = "imageObjectName")
public class CarImage {
    @ManyToOne
    @JsonBackReference
    private final Car associatedCar;
    @Id
    @GeneratedValue
    private Long id;
    private String imageObjectName;

    public CarImage(Car associatedCar, String imageObjectName) {
        this.associatedCar = associatedCar;
        this.imageObjectName = imageObjectName;
    }
}