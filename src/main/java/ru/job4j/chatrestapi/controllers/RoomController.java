package ru.job4j.chatrestapi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.job4j.chatrestapi.domain.Room;
import ru.job4j.chatrestapi.services.PersonService;
import ru.job4j.chatrestapi.services.RoomService;

import java.util.List;
import java.util.Optional;

/**
 * @author Roman Rusanov
 * @since 18.03.2021
 * email roman9628@gmail.com
 */
@RestController
@RequestMapping("/room")
public class RoomController {
    
    private final RoomService roomService;
    private final PersonService personService;

    public RoomController(RoomService roomService, PersonService personService) {
        this.roomService = roomService;
        this.personService = personService;
    }

    @GetMapping("/")
    public List<Room> findAll() {
        return this.roomService.getAllRooms();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> findById(@PathVariable Long id) {
        var room = this.roomService.getRoomById(id);
        return new ResponseEntity<Room>(
                room.orElse(new Room()),
                room.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @PostMapping("/")
    public ResponseEntity<Room> create(@RequestBody Room room) {
        return new ResponseEntity<Room>(
                this.roomService.createRoomAndMassagesInIt(room),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Room room) {
        if (this.roomService.isRoomPresent(room)) {
            this.roomService.updateRoomAndAddMessagesInIt(room);
            return ResponseEntity.ok().build();
        }
        return new ResponseEntity<Void>(
                HttpStatus.CONFLICT
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<Room> roomToDelete = this.roomService.getRoomById(id);
        if (roomToDelete.isPresent()) {
            this.personService.removeRoom(roomToDelete.get());
            this.roomService.deleteRoomAndMessageInItAndPersonMessage(id);
            return ResponseEntity.ok().build();
        }
        return new ResponseEntity<Void>(
                HttpStatus.CONFLICT
        );
    }
}