package ru.job4j.chatrestapi.controllers;

import ru.job4j.chatrestapi.domain.Message;
import ru.job4j.chatrestapi.domain.Person;
import ru.job4j.chatrestapi.domain.Room;
import ru.job4j.chatrestapi.repository.MessageRepository;
import ru.job4j.chatrestapi.repository.PersonRepository;
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
@RequestMapping("/message")
public class MessageController {

    private final MessageRepository messageRepository;
    private final PersonRepository personRepository;
    private final RoomRepository roomRepository;

    public MessageController(MessageRepository messageRepository, PersonRepository personRepository, RoomRepository roomRepository) {
        this.messageRepository = messageRepository;
        this.personRepository = personRepository;
        this.roomRepository = roomRepository;
    }

    @GetMapping("/")
    public List<Message> findAll() {
        return StreamSupport.stream(
                this.messageRepository.findAll().spliterator(), false
        ).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> findById(@PathVariable Long id) {
        var message = this.messageRepository.findById(id);
        return new ResponseEntity<Message>(
                message.orElse(new Message()),
                message.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @PostMapping("/")
    public ResponseEntity<Message> create(@RequestBody Message message) {
        Optional<Person> personMsgCreate = this.personRepository.findById(message.getPersonId());
        Optional<Room> roomToStoreMsg = this.roomRepository.findById(message.getRoomId());
        if (personMsgCreate.isPresent() && roomToStoreMsg.isPresent()) {
            Message response = this.messageRepository.save(message);
            personMsgCreate.get().addMessage(message);
            roomToStoreMsg.get().addMessage(message);
            this.personRepository.save(personMsgCreate.get());
            this.roomRepository.save(roomToStoreMsg.get());
            return new ResponseEntity<Message>(
                    response,
                    HttpStatus.CREATED
            );
        } else {
            return new ResponseEntity<Message>(
                    HttpStatus.CONFLICT
            );
        }
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Message message) {
        Optional<Message> messageFromRepository = this.messageRepository.findById(message.getId());
        Optional<Person> personMsgCreate = this.personRepository.findById(message.getPersonId());
        Optional<Room> roomToStoreMsg = this.roomRepository.findById(message.getRoomId());
        if (messageFromRepository.isPresent()
                && personMsgCreate.isPresent()
                && roomToStoreMsg.isPresent()) {
            if (isMessageChangeRoom(message, messageFromRepository.get())) {
                this.removeMsgFromRoom(messageFromRepository.get());
                messageFromRepository.get().setRoomId(message.getRoomId());
                roomToStoreMsg.get().addMessage(message);
                this.roomRepository.save(roomToStoreMsg.get());
            }
            if (isMessageChangePerson(message, messageFromRepository.get())) {
                this.removeMsgFromPerson(messageFromRepository.get());
                messageFromRepository.get().setPersonId(message.getPersonId());
                personMsgCreate.get().addMessage(message);
            } else {
                personMsgCreate.get().replaceMessage(message);
            }
            this.personRepository.save(personMsgCreate.get());
            messageFromRepository.get().setDescription(message.getDescription());
            this.messageRepository.save(messageFromRepository.get());
            return ResponseEntity.ok().build();
        } else {
            return new ResponseEntity<Void>(
                    HttpStatus.CONFLICT
            );
        }
    }

    private boolean isMessageChangeRoom(Message message, Message messageFromRepository) {
        return !message.getRoomId().equals(messageFromRepository.getRoomId());
    }

    private boolean isMessageChangePerson(Message message, Message messageFromRepository) {
        return !message.getPersonId().equals(messageFromRepository.getPersonId());
    }

    private void removeMsgFromRoom(Message messageFromRepository) {
        Long roomIdForMessageRemove = messageFromRepository.getRoomId();
        Room roomToRemoveMessage = this.roomRepository.findById(roomIdForMessageRemove).get();
        roomToRemoveMessage.removeMessage(messageFromRepository);
        this.roomRepository.save(roomToRemoveMessage);
    }

    private void removeMsgFromPerson(Message messageFromRepository) {
        Long personIdForMessageRemove = messageFromRepository.getPersonId();
        Person personToRemoveMessage = this.personRepository.findById(personIdForMessageRemove).get();
        personToRemoveMessage.removeMessage(messageFromRepository);
        this.personRepository.save(personToRemoveMessage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<Message> messageFromRepository = this.messageRepository.findById(id);
        if (messageFromRepository.isPresent()) {
            Optional<Room> roomToRemoveMsg = this.roomRepository.findById(messageFromRepository.get().getRoomId());
            Optional<Person> personMsgCreate = this.personRepository.findById(messageFromRepository.get().getPersonId());
            roomToRemoveMsg.get().removeMessage(messageFromRepository.get());
            this.roomRepository.save(roomToRemoveMsg.get());
            personMsgCreate.get().removeMessage(messageFromRepository.get());
            this.personRepository.save(personMsgCreate.get());
            this.messageRepository.delete(messageFromRepository.get());
            return ResponseEntity.ok().build();
        } else {
            return new ResponseEntity<Void>(
                    HttpStatus.CONFLICT
            );
        }
    }

}