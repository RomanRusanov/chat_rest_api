package ru.job4j.chatrestapi.controllers;

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
import ru.job4j.chatrestapi.domain.Message;
import ru.job4j.chatrestapi.services.MessageService;

import java.util.List;

/**
 * @author Roman Rusanov
 * @since 17.03.2021
 * email roman9628@gmail.com
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/")
    public List<Message> findAll() {
        return this.messageService.getAllMessages();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> findById(@PathVariable Long id) {
        var message = this.messageService.getMessageById(id);
        return new ResponseEntity<Message>(
                message.orElse(new Message()),
                message.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @PostMapping("/")
    public ResponseEntity<Message> create(@RequestBody Message message) {
        if (messageService.isPersonAndRoomExist(message)) {
            return new ResponseEntity<Message>(
                    messageService.addMessageAndPerson(message),
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
        if (this.messageService.isPersonAndRoomAndMessageExist(message)) {
            this.messageService.updateInMessageInPersonInRoom(message);
            return ResponseEntity.ok().build();
        } else {
            return new ResponseEntity<Void>(
                    HttpStatus.CONFLICT
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (this.messageService.isMessageExist(Message.of(id))) {
            this.messageService.deleteMessage(id);
            return ResponseEntity.ok().build();
        } else {
            return new ResponseEntity<Void>(
                    HttpStatus.CONFLICT
            );
        }
    }
}