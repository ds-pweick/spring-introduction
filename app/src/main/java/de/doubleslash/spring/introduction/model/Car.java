package de.doubleslash.spring.introduction.model;

import de.doubleslash.spring.introduction.spring.configuration.entity.Auditable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor(force = true)
@Data
@Entity
@Builder
public class Car extends Auditable {
    // jpa won't change these values after instantiation, can be final
    private final String brand;
    private final String model;
    @Id
    @GeneratedValue
    private Long id;
    @CreatedDate
    private Instant date;
    private String imageObjectName;

    public boolean equals(Car car) {
        return Objects.equals(this.id, car.getId()) && Objects.equals(this.brand, car.getBrand())
                && Objects.equals(this.model, car.getModel()) && Objects.equals(this.imageObjectName, car.getImageObjectName());
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ", \"brand\":\"" + brand + "\"" +
                ", \"model\":\"" + model + "\"" +
                ", \"date\":\"" + date + "\"" +
                ", \"imageObjectName\":\"" + imageObjectName + "\"" +
                "}";
    }

    public int hashCode() {
        return Objects.hash(id, model, brand);
    }
}