package de.doubleslash.spring.introduction.springcontexttests.controller;

/*
class PostCarInMemoryTest extends SpringInMemoryTest {
    @Autowired
    private CarRepository repository;
    @Autowired
    private CarDealershipController controller;

    @Test
    void givenCar_whenAddingCar_thenGetCar() {
        final Car car = Car.builder().model("TestModel").brand("TestBrand").build();

        repository.save(car);

        assertThat(repository.existsById(car.getId())).isTrue();
    }

    @Test
    void givenCars_whenRequestingCarReplacement_thenGetNewCar() throws Exception {
        final Car car = Car.builder().model("TestModel").brand("TestBrand").build();
        final Car secondCar = Car.builder().model("TestModel1").brand("TestBrand1").date(Instant.now())
                .carImageList(List.of()).build();
        final MockMultipartFile file = new MockMultipartFile("file", "TestTitle.png",
                MediaType.MULTIPART_FORM_DATA_VALUE, new byte[1]);

        Car savedCar = repository.save(car);
        assertThat(repository.existsById(savedCar.getId())).isTrue();
        controller.replaceCar(savedCar.getId(), secondCar.toString(), file);
        assertThat(repository.existsById(savedCar.getId())).isFalse();
    }

}*/
