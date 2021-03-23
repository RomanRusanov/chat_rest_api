package ru.job4j.chatrestapi.controllers;

import ru.job4j.chatrestapi.domain.Role;
import ru.job4j.chatrestapi.repository.RoleRepository;
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
import ru.job4j.chatrestapi.services.RoleService;

import java.util.List;

/**
 * @author Roman Rusanov
 * @since 18.03.2021
 * email roman9628@gmail.com
 */
@RestController
@RequestMapping("/role")
public class RoleController {
    
    private final RoleRepository roleRepository;
    private final RoleService roleService;

    public RoleController(RoleRepository roleRepository, RoleService roleService) {
        this.roleRepository = roleRepository;
        this.roleService = roleService;
    }

    @GetMapping("/")
    public List<Role> findAll() {
        return this.roleService.getAllRoles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> findById(@PathVariable Long id) {
        var role = this.roleService.getRoleById(id);
        return new ResponseEntity<Role>(
                role.orElse(new Role()),
                role.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @PostMapping("/")
    public ResponseEntity<Role> create(@RequestBody Role role) {
        return new ResponseEntity<Role>(
                this.roleService.createRole(role),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Role role) {
        if (this.roleService.isRoleNotExist(role)) {
            return new ResponseEntity<Void>(
                    HttpStatus.CONFLICT);
        }
        this.roleService.updateRole(role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Role role = Role.of(id);
        if (this.roleService.isRoleNotExist(role)) {
            return new ResponseEntity<Void>(
                    HttpStatus.CONFLICT);
        }
        this.roleService.deleteRoleAndPersonRole(id);
        return ResponseEntity.ok().build();
    }
}