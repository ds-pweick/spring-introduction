package de.doubleslash.spring.introduction.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import de.doubleslash.spring.introduction.spring.configuration.entity.Auditable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.List;
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
    @OneToMany(mappedBy = "associatedCar")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonManagedReference
    private List<CarImage> carImageList;

    public boolean equals(Car car) {
        return Objects.equals(this.id, car.getId()) && Objects.equals(this.brand, car.getBrand())
                && Objects.equals(this.model, car.getModel());
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ", \"brand\":\"" + brand + "\"" +
                ", \"model\":\"" + model + "\"" +
                ", \"date\":\"" + date + "\"" +
                ", \"carImageList\":" + carImageList + "}";
    }

    public int hashCode() {
        return Objects.hash(id, model, brand);
    }
}