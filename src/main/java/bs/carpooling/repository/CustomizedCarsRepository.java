package bs.carpooling.repository;

import bs.carpooling.entity.CarEntity;
import bs.carpooling.entity.GroupOfPeopleEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomizedCarsRepository {
  Mono<CarEntity> assignToCarWithAvailableSeats(GroupOfPeopleEntity group);

  Mono<GroupOfPeopleEntity> locateGroupById(Integer groupId);

  Mono<CarEntity> locateCarOfGroup(Integer groupId);

  Mono<CarEntity> putInWaitingQueue(GroupOfPeopleEntity group);

  Mono<GroupOfPeopleEntity> findWaitingById(Integer groupId);

  Flux<GroupOfPeopleEntity> findAllGroupsWaiting();

  Flux<CarEntity> findAllNotWaiting();

  Flux<GroupOfPeopleEntity> reassignOneWaitingGroup();

  Mono<CarEntity> dropoff(Integer groupId);
}
