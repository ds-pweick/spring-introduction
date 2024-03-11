package de.doubleslash.spring.introduction.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor(force = true)
@Value
@Entity
@Builder
public class Car {
    @Id
    @GeneratedValue
    Long id;
    String model;
    String brand;

    @CreatedDate
    Instant date;

    public boolean equals(Car car) {
        return hashCode() == car.hashCode();
    }

    public int hashCode() {
        return Objects.hash(id, model, brand);
    }

    @Override
    public String toString() {
        return "Car{" + "id=" + id + ", model='" + model + '\'' + ", brand='" + brand + '\'' + ", date=" + date + '}';
    }
}