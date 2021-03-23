package ru.job4j.chatrestapi.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Service;
import ru.job4j.chatrestapi.domain.Message;
import ru.job4j.chatrestapi.domain.Person;
import ru.job4j.chatrestapi.domain.Room;
import ru.job4j.chatrestapi.repository.MessageRepository;
import ru.job4j.chatrestapi.repository.PersonRepository;
import ru.job4j.chatrestapi.repository.RoomRepository;

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
public class MessageService {

    private final MessageRepository messageRepository;
    private final PersonRepository personRepository;
    private final RoomRepository roomRepository;
    private static final Logger LOG = LoggerFactory.getLogger(MessageService.class.getName());
    private static final Marker MARKER = MarkerFactory.getMarker("Service");

    public MessageService(MessageRepository messageRepository,
                          PersonRepository personRepository,
                          RoomRepository roomRepository) {
        this.messageRepository = messageRepository;
        this.personRepository = personRepository;
        this.roomRepository = roomRepository;
    }

    public List<Message> getAllMessages() {
        return StreamSupport.stream(
                this.messageRepository.findAll().spliterator(), false
        ).collect(Collectors.toList());
    }

    public Optional<Message> getMessageById(Long id) {
        return this.messageRepository.findById(id);
    }

    public boolean isMessageExist(Message message) {
        return this.messageRepository.findById(message.getId()).isPresent();
    }

    public boolean isPersonAndRoomExist(Message message) {
        return this.personRepository.findById(message.getPersonId()).isPresent()
                && this.roomRepository.findById(message.getRoomId()).isPresent();
    }

    public boolean isPersonAndRoomAndMessageExist(Message message) {
        return this.isPersonAndRoomExist(message) && this.isMessageExist(message);
    }

    public Message addMessageAndPerson(Message message) {
        Optional<Person> personMsgCreate = this.personRepository.findById(message.getPersonId());
        Optional<Room> roomToStoreMsg = this.roomRepository.findById(message.getRoomId());
        if (personMsgCreate.isEmpty() || roomToStoreMsg.isEmpty()) {
            LOG.error(MARKER, "Passed personId:{} or roomId:{} not exist in storage",
                    message.getPersonId(), message.getRoomId());
            return message;
        }
        Message result = this.messageRepository.save(message);
        personMsgCreate.get().addMessage(message);
        roomToStoreMsg.get().addMessage(message);
        this.personRepository.save(personMsgCreate.get());
        this.roomRepository.save(roomToStoreMsg.get());
        return result;
    }

    public void updateInMessageInPersonInRoom(Message message) {
        Optional<Message> messageFromRepository = this.messageRepository.findById(message.getId());
        Optional<Person> personMsgCreate = this.personRepository.findById(message.getPersonId());
        Optional<Room> roomToStoreMsg = this.roomRepository.findById(message.getRoomId());
        if (isMessageChangeRoom(message, messageFromRepository.get())) {
            this.removeMsgFromRoom(messageFromRepository.get());
            messageFromRepository.get().setRoomId(message.getRoomId());
            roomToStoreMsg.get().addMessage(message);
            this.roomRepository.save(roomToStoreMsg.get());
        }
        if (isMessageChangePerson(message, messageFromRepository.get())) {
            removeMsgFromPerson(messageFromRepository.get());
            messageFromRepository.get().setPersonId(message.getPersonId());
            personMsgCreate.get().addMessage(message);
        } else {
            personMsgCreate.get().replaceMessage(message);
        }
        this.personRepository.save(personMsgCreate.get());
        messageFromRepository.get().setDescription(message.getDescription());
        this.messageRepository.save(messageFromRepository.get());
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

    public void deleteMessage(Long id) {
        Optional<Message> messageFromRepository = this.messageRepository.findById(id);
        if (messageFromRepository.isPresent()) {
            Optional<Room> roomToRemoveMsg = this.roomRepository.findById(messageFromRepository.get().getRoomId());
            Optional<Person> personMsgCreate = this.personRepository.findById(messageFromRepository.get().getPersonId());
            roomToRemoveMsg.get().removeMessage(messageFromRepository.get());
            this.roomRepository.save(roomToRemoveMsg.get());
            personMsgCreate.get().removeMessage(messageFromRepository.get());
            this.personRepository.save(personMsgCreate.get());
            this.messageRepository.delete(messageFromRepository.get());
        } else {
            LOG.error(MARKER, "Passed message.id:{} not exist in storage", id);
        }
    }
}