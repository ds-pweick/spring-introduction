package de.doubleslash.spring.introduction.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.Objects;
@Setter
@Getter
@Entity
public class Car {
    @Id
    @GeneratedValue
    private long id;
    private String model;
    private String brand;
    @CreatedDate
    private Instant date;

    public Car() {

    }

    public Car(long id, String model, String brand, Instant date) {
        this.id = id;
        this.model = model;
        this.brand = brand;
        this.date = date;
    }

    public boolean equals(Car car) {
        return id == car.id && model.equals(car.model) && brand.equals(car.brand);
    }

    public int hashCode() {
        return Objects.hash(id, model, brand);
    }

    @Override
    public String toString() {
        return "Car{" + "id=" + id + ", model='" + model + '\'' + ", brand='" + brand + '\'' + ", date=" + date + '}';
    }
}