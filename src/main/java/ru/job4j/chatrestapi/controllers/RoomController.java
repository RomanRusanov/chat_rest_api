package ru.job4j.chatrestapi.controllers;

import ru.job4j.chatrestapi.domain.Message;
import ru.job4j.chatrestapi.domain.Person;
import ru.job4j.chatrestapi.domain.Room;
import ru.job4j.chatrestapi.repository.MessageRepository;
import ru.job4j.chatrestapi.repository.PersonRepository;
import ru.job4j.chatrestapi.repository.RoomRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Roman Rusanov
 * @since 18.03.2021
 * email roman9628@gmail.com
 */
@RestController
@RequestMapping("/room")
public class RoomController {
    
    private final RoomRepository roomRepository;
    private final PersonRepository personRepository;
    private final MessageRepository messageRepository;

    public RoomController(RoomRepository roomRepository, PersonRepository personRepository, MessageRepository messageRepository) {
        this.roomRepository = roomRepository;
        this.personRepository = personRepository;
        this.messageRepository = messageRepository;
    }

    @GetMapping("/")
    public List<Room> findAll() {
        return StreamSupport.stream(
                this.roomRepository.findAll().spliterator(), false
        ).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> findById(@PathVariable Long id) {
        var room = this.roomRepository.findById(id);
        return new ResponseEntity<Room>(
                room.orElse(new Room()),
                room.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @PostMapping("/")
    public ResponseEntity<Room> create(@RequestBody Room room) {
        Room createdRoom = this.roomRepository.save(Room.of(-1L, room.getName()));
        for (Message message : room.getMessages()) {
            if (isMessageExist(message) && isPersonPresent(message)) {
                createdRoom.addMessage(message);
                Person personToAddRoom = this.personRepository.findById(message.getPersonId()).get();
                personToAddRoom.addRoom(createdRoom);
                this.personRepository.save(personToAddRoom);
            } else {
                return new ResponseEntity<Room>(
                        HttpStatus.CONFLICT
                );
            }
        }
        return new ResponseEntity<Room>(
                this.roomRepository.save(createdRoom),
                HttpStatus.CREATED
        );
    }

    public boolean isMessageExist(Message message) {
        return this.messageRepository.findById(message.getId()).isPresent();
    }

    public boolean isPersonPresent(Message message) {
        return this.personRepository.findById(message.getPersonId()).isPresent();
    }

    public boolean isRoomPresent(Room room) {
        return this.roomRepository.findById(room.getId()).isPresent();
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Room room) {
        if (isRoomPresent(room)) {
            Room roomFromRepository = this.roomRepository.findById(room.getId()).get();
            roomFromRepository.setName(room.getName());
            for (Message message : room.getMessages()) {
                if (isMessageExist(message) && isPersonPresent(message)) {
                    roomFromRepository.addMessage(message);
                    Person personToAddRoom = this.personRepository.findById(message.getPersonId()).get();
                    personToAddRoom.addRoom(roomFromRepository);
                    this.personRepository.save(personToAddRoom);
                } else {
                    return new ResponseEntity<Void>(
                            HttpStatus.CONFLICT
                    );
                }
            }
            this.roomRepository.save(roomFromRepository);
            return ResponseEntity.ok().build();
        }
        return new ResponseEntity<Void>(
                HttpStatus.CONFLICT
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<Room> roomToDelete = this.roomRepository.findById(id);
        if (roomToDelete.isPresent()) {
            roomToDelete.get().getMessages().forEach(message -> {
                Optional<Person> personToRemoveMsg = this.personRepository.findById(message.getPersonId());
                if (personToRemoveMsg.isPresent()) {
                    List<Message> msgToDelete = new ArrayList<>();
                    personToRemoveMsg.get().getMessages().forEach(msgPerson -> {
                        if (msgPerson.getRoomId().equals(message.getRoomId())) {
                            msgToDelete.add(msgPerson);
                        }
                    });
                    personToRemoveMsg.get().getMessages().removeAll(msgToDelete);
                    personToRemoveMsg.get().removeRoom(roomToDelete.get());
                    this.personRepository.save(personToRemoveMsg.get());
                }
            });
            List<Message> messageToRemove = new ArrayList<Message>(roomToDelete.get().getMessages());
            roomToDelete.get().removeAllMessages();
            this.roomRepository.save(roomToDelete.get());
            this.messageRepository.deleteAll(messageToRemove);
            this.roomRepository.delete(roomToDelete.get());
            return ResponseEntity.ok().build();
        }
        return new ResponseEntity<Void>(
                HttpStatus.CONFLICT
        );
    }
}