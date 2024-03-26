package de.doubleslash.spring.introduction.springcontexttests.controller;


/*@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:springintroduction",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DeleteCarInMemoryTest extends SpringInMemoryTest {

    @Test
    void givenCar_whenDeletingCar_thenDeleteCarAndAssociatedImages() {
        final String carString = "{\"brand\":\"TestBrand\",\"model\":\"TestModel\"}";
        final MockMultipartFile file = new MockMultipartFile("file", "TestTitle.png",
                MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        controller.addCarAndImage(carString, file);

        Car car = carRepository.findAll().get(0);
        assertThat(car).isNotNull();
        assertThat(carImageRepository.findAllByAssociatedCarId(car.getId())).isNotEmpty();
    }

controller.deleteCar(car.getId());
        assertThat(carRepository.existsById(car.getId())).isFalse();
        assertThat(carImageRepository.findAllByAssociatedCarId(car.getId())).isEmpty();*//*

    }

    @Test
    void givenCarsOfBrand_whenDeletingCarsByBrand_thenDeleteCars() throws Exception {

        final List<Car> carList = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                Car car = Car.builder().model("TestModel%d".formatted(i)).brand("TestBrand%d".formatted(i))
                        .carImageList(List.of()).build();
                carList.add(car);
            }
        }

        carRepository.saveAll(carList);
        //controller.deleteCarByBrand("TestBrand0");

        assertThat(carRepository.findAll()).allMatch(car -> car.getBrand().equals("TestBrand1"));
    }
}*/
