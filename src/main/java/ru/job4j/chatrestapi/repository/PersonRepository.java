package ru.job4j.chatrestapi.repository;

import ru.job4j.chatrestapi.domain.Person;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Roman Rusanov
 * @since 17.03.2021
 * email roman9628@gmail.com
 */
public interface PersonRepository extends CrudRepository<Person, Long> {
    Person getPersonByUsername(String username);
}