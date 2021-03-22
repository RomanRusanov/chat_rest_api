package ru.job4j.chatrestapi.repository;

import ru.job4j.chatrestapi.domain.Role;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Long> {
}
