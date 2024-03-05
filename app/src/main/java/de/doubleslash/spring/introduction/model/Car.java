package de.doubleslash.spring.introduction.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.Objects;

public class Car {
    @Id
    @GeneratedValue
    private long id;
    private String model;
    private String brand;
    @CreatedDate
    private Instant date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
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