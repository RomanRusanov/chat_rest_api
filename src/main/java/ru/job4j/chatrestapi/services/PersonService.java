package ru.job4j.chatrestapi.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Service;
import ru.job4j.chatrestapi.domain.Message;
import ru.job4j.chatrestapi.domain.Person;
import ru.job4j.chatrestapi.domain.Role;
import ru.job4j.chatrestapi.repository.MessageRepository;
import ru.job4j.chatrestapi.repository.PersonRepository;
import ru.job4j.chatrestapi.repository.RoleRepository;
import ru.job4j.chatrestapi.repository.RoomRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Roman Rusanov
 * @since 23.03.2021
 * email roman9628@gmail.com
 */
@Service
public class PersonService {

    private final MessageRepository messageRepository;
    private final PersonRepository personRepository;
    private final RoomRepository roomRepository;
    private final RoleRepository roleRepository;
    private static final Logger LOG = LoggerFactory.getLogger(PersonService.class.getName());
    private static final Marker MARKER = MarkerFactory.getMarker("Service");

    public PersonService(MessageRepository messageRepository,
                         PersonRepository personRepository,
                         RoomRepository roomRepository,
                         RoleRepository roleRepository) {
        this.messageRepository = messageRepository;
        this.personRepository = personRepository;
        this.roomRepository = roomRepository;
        this.roleRepository = roleRepository;
    }

    public List<Person> getAllPersons() {
        return StreamSupport.stream(
                this.personRepository.findAll().spliterator(), false
        ).collect(Collectors.toList());
    }

    public Optional<Person> getPersonById(Long id) {
        return this.personRepository.findById(id);
    }

    public Person createPersonAndOtherItPass(Person person) {
        person.getRoles().forEach(this.roleRepository::save);
        person.getRooms().forEach(this.roomRepository::save);
        return this.personRepository.save(person);
    }

    public boolean isPersonExistByName(Person person) {
        return this.personRepository.getPersonByUsername(person.getUsername()) == null;
    }

    public boolean isPersonExist(Person person) {
        return this.personRepository.findById(person.getId()).isPresent();
    }

    public void updatePersonAndOtherItPass(Person person) {
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
        } else {
            LOG.error(MARKER, "Passed person:{} not exist in storage", person);
        }
    }

    public void deletePersonAndItMsgAndRemoveItFromRooms(Person person) {
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
        } else {
            LOG.error(MARKER, "Passed person:{} not exist in storage", person);
        }
    }

    public List<Person> getAllPersonWithRoleId(Long id) {
        return this.personRepository.getAllByRoles(Role.of(id));
    }

    public Person findPersonByUsername(String name) {
        return this.personRepository.getPersonByUsername(name);
    }
}