package ru.job4j.chatrestapi.controllers;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.job4j.chatrestapi.domain.Person;
import ru.job4j.chatrestapi.services.PersonService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private PersonService users;
    private BCryptPasswordEncoder encoder;

    public UserController(PersonService users,
                          BCryptPasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @PostMapping("/sign-up")
    public void signUp(@RequestBody Person person) {
        person.setPassword(encoder.encode(person.getPassword()));
        users.createPersonAndOtherItPass(person);
    }

    @GetMapping("/all")
    public List<Person> findAll() {
        return users.getAllPersons();
    }
}