package com.example.jwtauth.service;

import com.example.jwtauth.repository.MessageRepository;
import com.example.jwtauth.user.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;
    public Message sendMessage(Message message) {
        message.setSentAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public List<Message> getInbox(String receiverEmail) {
        return messageRepository.findByReceiverEmailAndIsDraftFalseAndIsTrashedFalse(receiverEmail);
    }


    public List<Message> getSend(String senderEmail) {
        return messageRepository.findBySenderEmail(senderEmail);
    }

    public List<Message> getStarred(String receiverEmail) {
        return messageRepository.findByReceiverEmailAndIsStarredTrue(receiverEmail);
    }

    public List<Message> getDrafts(String senderEmail) {
        return messageRepository.findBySenderEmailAndIsDraftTrue(senderEmail);
    }

    public List<Message> getTrash(String receiverEmail) {
        return messageRepository.findByReceiverEmailAndIsTrashedTrue(receiverEmail);
    }

    public Message toggleStart(String messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow();
        message.setStarred(!message.isStarred());
        return messageRepository.save(message);
    }


    public Message getMessageById(String id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));
    }
}
