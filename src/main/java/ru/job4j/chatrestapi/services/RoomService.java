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
public class RoomService {

    private final RoomRepository roomRepository;
    private final PersonRepository personRepository;
    private final MessageRepository messageRepository;
    private final MessageService messageService;
    private static final Logger LOG = LoggerFactory.getLogger(RoleService.class.getName());
    private static final Marker MARKER = MarkerFactory.getMarker("Service");

    public RoomService(RoomRepository roomRepository,
                       PersonRepository personRepository,
                       MessageRepository messageRepository,
                       MessageService messageService) {
        this.roomRepository = roomRepository;
        this.personRepository = personRepository;
        this.messageRepository = messageRepository;
        this.messageService = messageService;
    }

    public List<Room> getAllRooms() {
        return StreamSupport.stream(
                this.roomRepository.findAll().spliterator(), false
        ).collect(Collectors.toList());
    }

    public Optional<Room> getRoomById(Long id) {
        return this.roomRepository.findById(id);
    }

    public boolean isMessageNotExist(Message message) {
        return this.messageRepository.findById(message.getId()).isEmpty();
    }

    public boolean isPersonPresent(Message message) {
        return this.personRepository.findById(message.getPersonId()).isPresent();
    }

    public Room createRoomAndMassagesInIt(Room room) {
        Room createdRoom = this.roomRepository.save(Room.of(-1L, room.getName()));
        saveAllMessages(room, createdRoom);
        return this.roomRepository.save(createdRoom);
    }

    public void updateRoomAndAddMessagesInIt(Room room) {
        if (isRoomPresent(room)) {
            Room roomFromRepository = this.roomRepository.findById(room.getId()).get();
            roomFromRepository.setName(room.getName());
            saveAllMessages(room, roomFromRepository);
            this.roomRepository.save(roomFromRepository);
        }
        LOG.error(MARKER, "Passed room:{} not exist in storage, can't update!", room);
    }

    private void saveAllMessages(Room room, Room roomFromRepository) {
        for (Message message : room.getMessages()) {
            if (isMessageNotExist(message) && isPersonPresent(message)) {
                this.messageService.addMessageAndPerson(message);
                roomFromRepository.addMessage(message);
                Person personToAddRoom = this.personRepository.findById(message.getPersonId()).get();
                personToAddRoom.addRoom(roomFromRepository);
                this.personRepository.save(personToAddRoom);
            } else {
                LOG.error(MARKER, "Passed room:{} contain message:{} already exist in storage, message be ignored!",
                        room, message);
            }
        }
    }

    public boolean isRoomPresent(Room room) {
        return this.roomRepository.findById(room.getId()).isPresent();
    }

    public void deleteRoomAndMessageInItAndPersonMessage(Long id) {
        Optional<Room> roomToDelete = this.roomRepository.findById(id);
        if (roomToDelete.isPresent()) {
            roomToDelete.get().getMessages().forEach(message -> {
                Optional<Person> personToRemoveMsg = this.personRepository.findById(message.getPersonId());
                if (personToRemoveMsg.isPresent()) {
                    List<Message> msgToDelete = new ArrayList<>();
                    personToRemoveMsg.get().getMessages().forEach(msgPerson -> {
                        if (msgPerson.getRoomId().equals(message.getRoomId())) {
                            msgToDelete.add(msgPerson);
                        }
                    });
                    personToRemoveMsg.get().getMessages().removeAll(msgToDelete);
                    personToRemoveMsg.get().removeRoom(roomToDelete.get());
                    this.personRepository.save(personToRemoveMsg.get());
                }
            });
            List<Message> messageToRemove = new ArrayList<Message>(roomToDelete.get().getMessages());
            roomToDelete.get().removeAllMessages();
            this.roomRepository.save(roomToDelete.get());
            this.messageRepository.deleteAll(messageToRemove);
            this.roomRepository.delete(roomToDelete.get());
        }
        LOG.error(MARKER, "Passed roomId:{} not exist in storage, can't delete!", id);
    }

    public boolean isRoomPresentById(Long id) {
        return this.roomRepository.findById(id).isPresent();
    }
}