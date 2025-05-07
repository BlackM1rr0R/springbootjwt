package com.example.jwtauth.repository;

import com.example.jwtauth.user.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findByReceiverEmail(String receiverEmail);

    List<Message> findBySenderEmail(String senderEmail);

    List<Message> findByReceiverEmailAndIsStarredTrue(String receiverEmail);

    List<Message> findBySenderEmailAndIsDraftTrue(String senderEmail);

    List<Message> findByReceiverEmailAndIsTrashedTrue(String receiverEmail);

    List<Message> findByReceiverEmailAndIsDraftFalseAndIsTrashedFalse(String receiverEmail);

    List<Message> findByReceiverEmailAndSnoozedTrue(String email);
}
