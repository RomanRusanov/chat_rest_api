package ru.job4j.chatrestapi.repository;

import ru.job4j.chatrestapi.domain.Room;
import org.springframework.data.repository.CrudRepository;

public interface RoomRepository extends CrudRepository<Room, Long> {
}