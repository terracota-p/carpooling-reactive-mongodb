package com.cabify.pooling.service;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.cabify.pooling.dto.CarDTO;
import com.cabify.pooling.dto.GroupOfPeopleDTO;
import com.cabify.pooling.entity.CarEntity;
import com.cabify.pooling.entity.GroupOfPeopleEntity;
import com.cabify.pooling.exception.GroupAlreadyExistsException;
import com.cabify.pooling.repository.CarsRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataMongoTest
@RunWith(SpringRunner.class)
public class CarPoolingServiceTest {

	@Autowired
	private CarsRepository carsRepository;

	private CarPoolingService carPoolingService;

	@Before
	public void before() {
		carPoolingService = new CarPoolingService(carsRepository);
	}

	@Test
	public void GivenCarWithAvailableSeats_WhenJourney_ThenCarAsigned() throws GroupAlreadyExistsException {
		CarDTO expectedCar = new CarDTO(1, 3);
		carPoolingService.createCars(Arrays.asList(expectedCar)).blockLast();

		GroupOfPeopleDTO requestedGroup = new GroupOfPeopleDTO(1, 2);
		Mono<CarEntity> result = carPoolingService.journey(requestedGroup);

		GroupOfPeopleEntity expectedGroup = new GroupOfPeopleEntity(requestedGroup.getId(), requestedGroup.getPeople());
		StepVerifier.create(result).expectNextMatches(
				assignedCar -> expectedCar.getId() == assignedCar.getId() &&
				expectedCar.getSeats() - requestedGroup.getPeople() == assignedCar.getSeatsAvailable() &&
				assignedCar.getGroups().size() == 1 &&
				assignedCar.getGroups().contains(expectedGroup)).verifyComplete();
	}

	@Test
	public void GivenCarsWithAvailableSeats_WhenJourney_ThenCarAsignedWithLeastNeededAvailableSeats() throws GroupAlreadyExistsException {
		CarDTO expectedCar = new CarDTO(3, 3);
		carPoolingService.createCars(Arrays.asList(new CarDTO(1, 1), new CarDTO(2, 6), expectedCar)).blockLast();

		GroupOfPeopleDTO requestedGroup = new GroupOfPeopleDTO(1, 2);
		Mono<CarEntity> result = carPoolingService.journey(requestedGroup);

		GroupOfPeopleEntity expectedGroup = new GroupOfPeopleEntity(requestedGroup.getId(), requestedGroup.getPeople());
		StepVerifier.create(result).expectNextMatches(assignedCar -> expectedCar.getId() == assignedCar.getId()
				&& expectedCar.getSeats() - requestedGroup.getPeople() == assignedCar.getSeatsAvailable()
				&& assignedCar.getGroups().size() == 1
				&& assignedCar.getGroups().contains(expectedGroup)).verifyComplete();
	}

	@Test
	public void GivenCarsWithoutEnoughAvailableSeats_WhenJourney_ThenCarUnasigned() throws GroupAlreadyExistsException {
		carPoolingService.createCars(Arrays.asList(new CarDTO(1, 3)))
			.then(carPoolingService.journey(new GroupOfPeopleDTO(1, 2))).block();

		GroupOfPeopleDTO requestedGroup = new GroupOfPeopleDTO(2, 2);
		Mono<CarEntity> result = carPoolingService.journey(requestedGroup);

		StepVerifier.create(result).verifyComplete();
	}

	@Test
	public void GivenCarAssigned_WhenDropoff_ThenSeatsFreed() throws GroupAlreadyExistsException {
		CarDTO expectedCar = new CarDTO(1, 3);
		GroupOfPeopleDTO requestedGroup = new GroupOfPeopleDTO(1, 2);
		Mono<CarEntity> given = carPoolingService.createCars(Arrays.asList(expectedCar))
			.then(carPoolingService.journey(requestedGroup));

		Mono<CarEntity> result = given.then(carPoolingService.dropoff(requestedGroup.getId()));

		StepVerifier.create(result).expectNextMatches(droppedCar -> expectedCar.getSeats() == droppedCar.getSeatsAvailable())
				.verifyComplete();
	}

	@Test
	public void GivenGroupAssigned_AndDroppedoff_WhenLocate_ThenGroupNotFound() throws Exception {
		CarDTO expectedCar = new CarDTO(1, 3);
		GroupOfPeopleDTO requestedGroup = new GroupOfPeopleDTO(1, 2);
		Mono<CarEntity> given = carPoolingService.createCars(Arrays.asList(expectedCar))
			.then(carPoolingService.journey(requestedGroup))
			.then(carPoolingService.dropoff(requestedGroup.getId()));

		Mono<GroupOfPeopleEntity> result = given.then(carPoolingService.findGroup(requestedGroup.getId()));

		StepVerifier.create(result).verifyComplete();
	}

}
