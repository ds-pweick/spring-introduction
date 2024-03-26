/*package de.doubleslash.spring.introduction.springcontexttests.controller;

import de.doubleslash.spring.introduction.model.Car;
import de.doubleslash.spring.introduction.repository.CarRepository;
import de.doubleslash.spring.introduction.springcontexttests.setup.SpringInMemoryTest;
import org.springframework.beans.factory.annotation.Autowired;


class GetCarInMemoryTest extends SpringInMemoryTest {
    private final Car car = Car.builder().id(1L).model("TestModel").brand("TestBrand").build();
    @Autowired
    private CarRepository carRepository;*/


    /*void givenCar_whenRequestingCar_thenGetCar() {

        final long id = carRepository.save(car).getId();

        template.exchange(CarD, HttpMethod.GET,
                new HttpEntity<>(getDefaultHeaders()), Car.class);


        assertThat(controller.allCars().getBody()).hasSameSizeAs(carList)
                .allMatch(car -> carList.stream().anyMatch(car1 -> car1.equals(car)));
    }*/

    /*@Test
    void givenCar_whenRequestingCar_thenGetCar() {
        //final long id = carRepository.save(car).getId();
        //

        Car response = new TestRestTemplate().
                getForEntity("http://localhost:9092/cars/" + id, Car.class).getBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(car.getId());
    }

}*/
