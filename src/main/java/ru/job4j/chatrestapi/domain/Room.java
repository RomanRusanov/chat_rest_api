package ru.job4j.chatrestapi.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Roman Rusanov
 * @since 17.03.2021
 * email roman9628@gmail.com
 */
@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToMany()
    @JoinTable(name = "room_message",
            joinColumns = {@JoinColumn(name = "room_id")},
            inverseJoinColumns = {@JoinColumn(name = "message_id")})
    private List<Message> messages = new ArrayList<>();

    public static Room of(Long id, String name) {
        Room room = new Room();
        room.id = id;
        room.name = name;
        return room;
    }

    public Message findMessageById(Long id) {
        return this.messages.stream()
                .filter((message -> message.getId().equals(id)))
                .findFirst().orElseThrow(IllegalStateException::new);
    }

    public void replaceMessage(Message message) {
        int index = this.messages.indexOf(message);
        this.messages.set(index, message);
    }

    public void removeMessage(Message message) {
        this.messages.remove(message);
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void removeAllMessages() {
        this.messages.clear();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Room room = (Room) o;
        return Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Room{"
                + "id=" + id
                + ", name='" + name + '\''
                + '}';
    }
}