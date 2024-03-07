package de.doubleslash.spring.introduction.model;

import lombok.Builder;

@Builder
public class CarCheckMappingRequest {
    public Car firstCar;
    public Car secondCar;
}
