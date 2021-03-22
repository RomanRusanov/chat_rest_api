package ru.job4j.chatrestapi.controllers;

import ru.job4j.chatrestapi.domain.Message;
import ru.job4j.chatrestapi.domain.Person;
import ru.job4j.chatrestapi.repository.MessageRepository;
import ru.job4j.chatrestapi.repository.PersonRepository;
import ru.job4j.chatrestapi.repository.RoleRepository;
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
 * @since 17.03.2021
 * email roman9628@gmail.com
 */
@RestController
@RequestMapping("/person")
public class PersonController {

    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;

    public PersonController(PersonRepository personRepository,
                            RoleRepository roleRepository,
                            RoomRepository roomRepository,
                            MessageRepository messageRepository) {
        this.personRepository = personRepository;
        this.roleRepository = roleRepository;
        this.roomRepository = roomRepository;
        this.messageRepository = messageRepository;
    }

    @GetMapping("/")
    public List<Person> findAll() {
        return StreamSupport.stream(
                this.personRepository.findAll().spliterator(), false
        ).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable Long id) {
        var person = this.personRepository.findById(id);
        return new ResponseEntity<Person>(
                person.orElse(new Person()),
                person.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @PostMapping("/")
    public ResponseEntity<Person> create(@RequestBody Person person) {
        Person response;
        if (this.personRepository.getPersonByUsername(person.getUsername()) == null) {
            person.getRoles().forEach(this.roleRepository::save);
            person.getRooms().forEach(this.roomRepository::save);
            response = this.personRepository.save(person);
            return new ResponseEntity<Person>(
                    response,
                    HttpStatus.CREATED
            );
        } else {
            return new ResponseEntity<Person>(
                    HttpStatus.CONFLICT
            );
        }
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Person person) {
        Optional<Person> personFromRepository = this.personRepository.findById(person.getId());
        if (personFromRepository.isPresent()) {
            person.getRoles().forEach((role) -> {
                if (!personFromRepository.get().getRoles().contains(role)) {
                    this.roleRepository.save(role);
                    personFromRepository.get().addRole(role);
                }
            });
            person.getRooms().forEach((room) -> {
                if (!personFromRepository.get().getRooms().contains(room)) {
                    this.roomRepository.save(room);
                    personFromRepository.get().addRoom(room);
                }
            });
            person.getMessages().forEach((message) -> {
                if (!personFromRepository.get().getMessages().contains(message)) {
                    this.messageRepository.save(message);
                    personFromRepository.get().addMessage(message);
                }
            });
            personFromRepository.get().setUsername(person.getUsername());
            personFromRepository.get().setPassword(person.getPassword());
            this.personRepository.save(personFromRepository.get());
            return ResponseEntity.ok().build();
        } else {
            return new ResponseEntity<Void>(
                    HttpStatus.CONFLICT
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Person person = new Person();
        person.setId(id);
        Optional<Person> personFromRepository = this.personRepository.findById(person.getId());
        if (personFromRepository.isPresent()) {
            personFromRepository.get().removeAllRoles();
            personFromRepository.get().getRooms().forEach((room -> {
                room.removeAllMessages();
                this.roomRepository.save(room);
            }));
            personFromRepository.get().removeAllRooms();
            List<Message> messagesToDelete = new ArrayList<>(personFromRepository.get().getMessages());
            personFromRepository.get().removeAllMessage();
            this.personRepository.save(personFromRepository.get());
            this.messageRepository.deleteAll(messagesToDelete);
            this.personRepository.delete(person);
            return ResponseEntity.ok().build();
        } else {
            return new ResponseEntity<Void>(
                    HttpStatus.CONFLICT
            );
        }
    }

}