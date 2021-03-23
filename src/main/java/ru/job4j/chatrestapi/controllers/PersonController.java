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
import ru.job4j.chatrestapi.domain.Person;
import ru.job4j.chatrestapi.services.PersonService;

import java.util.List;

/**
 * @author Roman Rusanov
 * @since 17.03.2021
 * email roman9628@gmail.com
 */
@RestController
@RequestMapping("/person")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/")
    public List<Person> findAll() {
        return this.personService.getAllPersons();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable Long id) {
        var person = this.personService.getPersonById(id);
        return new ResponseEntity<Person>(
                person.orElse(new Person()),
                person.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @PostMapping("/")
    public ResponseEntity<Person> create(@RequestBody Person person) {
        if (this.personService.isPersonExistByName(person)) {
            return new ResponseEntity<Person>(
                    this.personService.createPersonAndOtherItPass(person),
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
        if (this.personService.isPersonExist(person)) {
            this.personService.updatePersonAndOtherItPass(person);
            return ResponseEntity.ok().build();
        } else {
            return new ResponseEntity<Void>(
                    HttpStatus.CONFLICT
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Person person = Person.of(id);
        if (this.personService.isPersonExist(person)) {
            this.personService.deletePersonAndItMsgAndRemoveItFromRooms(person);
            return ResponseEntity.ok().build();
        } else {
            return new ResponseEntity<Void>(
                    HttpStatus.CONFLICT
            );
        }
    }
}