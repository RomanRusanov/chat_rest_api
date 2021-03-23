package ru.job4j.chatrestapi.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Service;
import ru.job4j.chatrestapi.domain.Role;
import ru.job4j.chatrestapi.repository.PersonRepository;
import ru.job4j.chatrestapi.repository.RoleRepository;

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
public class RoleService {

    private final RoleRepository roleRepository;
    private final PersonService personService;
    private final PersonRepository personRepository;
    private static final Logger LOG = LoggerFactory.getLogger(RoleService.class.getName());
    private static final Marker MARKER = MarkerFactory.getMarker("Service");

    public RoleService(RoleRepository roleRepository,
                       PersonService personService,
                       PersonRepository personRepository) {
        this.roleRepository = roleRepository;
        this.personService = personService;
        this.personRepository = personRepository;
    }

    public List<Role> getAllRoles() {
        return StreamSupport.stream(
                this.roleRepository.findAll().spliterator(), false
        ).collect(Collectors.toList());
    }

    public Optional<Role> getRoleById(Long id) {
        return this.roleRepository.findById(id);
    }

    public Role createRole(Role role) {
        return this.roleRepository.save(role);
    }

    public void updateRole(Role role) {
        Optional<Role> roleFromRepository = this.roleRepository.findById(role.getId());
        if (roleFromRepository.isPresent()) {
            this.roleRepository.save(role);
        } else {
            LOG.error(MARKER, "Passed role:{} not exist in storage, can't update!", role);
        }
    }

    public boolean isRoleNotExist(Role role) {
        return  this.roleRepository.findById(role.getId()).isEmpty();
    }

    public void deleteRoleAndPersonRole(Long id) {
        Optional<Role> roleFromRepository = this.roleRepository.findById(id);
        if (roleFromRepository.isPresent()) {
            this.personService.getAllPersonWithRoleId(id).forEach(person -> {
                person.removeRole(roleFromRepository.get());
                personRepository.save(person);
            });
            this.roleRepository.delete(roleFromRepository.get());
        } else {
            LOG.error(MARKER, "Passed roleId:{} not exist in storage, can't delete!", id);
        }
    }
}