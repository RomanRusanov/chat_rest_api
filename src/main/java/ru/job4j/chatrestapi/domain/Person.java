package ru.job4j.chatrestapi.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Roman Rusanov
 * @since 17.03.2021
 * email roman9628@gmail.com
 */
@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    @OneToMany(orphanRemoval = true)
    @JoinTable(name = "person_message",
            joinColumns = {@JoinColumn(name = "person_id")},
            inverseJoinColumns = {@JoinColumn(name = "message_id")})
    private List<Message> messages = new ArrayList<>();
    @ManyToMany()
    @JoinTable(name = "person_role",
            joinColumns = {@JoinColumn(name = "person_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})
    private List<Role> roles = new ArrayList<>();
    @ManyToMany()
    @JoinTable(name = "room_person",
            joinColumns = {@JoinColumn(name = "person_id")},
            inverseJoinColumns = {@JoinColumn(name = "room_id")})
    private List<Room> rooms = new ArrayList<>();

    public static Person of(Long id, String username, String password) {
        Person person = new Person();
        person.id = id;
        person.username = username;
        person.password = password;
        return person;
    }


    public void removeMessage(Message message) {
        this.messages.remove(message);
    }

    public void replaceMessage(Message message) {
        int index = this.messages.indexOf(message);
        this.messages.set(index, message);
    }

    public void addRoom(Room room) {
        this.rooms.add(room);
    }

    public void removeRoom(Room room) {
        this.rooms.remove(room);
    }

    public void removeAllRooms() {
        this.rooms.clear();
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeAllRoles() {
            this.roles.clear();
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void removeAllMessage() {
        this.messages.clear();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> allMessage) {
        this.messages = allMessage;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> allRoles) {
        this.roles = allRoles;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> allRooms) {
        this.rooms = allRooms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Person person = (Person) o;
        return Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Person{"
                + "id=" + id
                + ", username='" + username + '\''
                + ", password='" + password + '\''
                + ", Messages='" + messages + '\''
                + ", Rooms='" + rooms + '\''
                + ", Roles='" + roles + '\''
                + '}';
    }

}