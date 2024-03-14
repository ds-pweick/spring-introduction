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
    @Id
    @GeneratedValue
    private Long id;
    // jpa won't change the value after instantiation, can be final
    private final String brand;
    private final String model;
    @CreatedDate
    private Instant date;

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