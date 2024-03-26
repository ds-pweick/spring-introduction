package de.doubleslash.spring.introduction.springcontexttests.controller;

/*
public class GenericDbBehaviorCheckTest extends SpringInMemoryTest {

    @Autowired
    private CarRepository repository;

    @Test
    void givenDatabase_whenHeavyAccessLoad_thenCheckIfPerformanceConsistent() {
        final List<Car> carList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Car car = Car.builder().model("TestModel").brand("TestBrand").build();
            carList.add(car);
        }

        repository.saveAll(carList);

        // make sure all "clients" can actually retrieve the desired information
        Arrays.stream(new int[10000]).parallel().forEach(i -> {
            List<Car> result = repository.findAll();
            assertThat(result).hasSameSizeAs(carList);
            assertThat(result).allMatch(car -> carList.stream().anyMatch(car1 -> car1.equals(car)));
        });
    }

    @Test
    void givenDatabase_whenCreatingMultipleCarsInParallel_thenCheckIfPerformanceConsistent() {
        final List<Car> carList = new ArrayList<>();

        final int numOfCars = 10000;

        for (int i = 0; i < numOfCars; i++) {
            Car car = Car.builder().id((long) i).model("TestModel").brand("TestBrand").build();
            carList.add(car);
        }

        Arrays.stream(new int[numOfCars]).parallel().forEach(i -> repository.save(carList.get(i)));

        // make sure all cars created in parallel were actually created
        final List<Car> obtainedList = repository.findAll();
        assertThat(obtainedList).hasSameSizeAs(carList);
    }

}*/
