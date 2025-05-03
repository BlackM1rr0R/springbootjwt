package com.example.jwtauth.controller;

import com.example.jwtauth.jwtutil.JwtUtil;
import com.example.jwtauth.service.MessageService;
import com.example.jwtauth.user.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private JwtUtil jwtUtil;

    // Yardımcı method: Token'dan email çıkart
    private String extractEmailFromHeader(String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return jwtUtil.getUsernameFromToken(token);
    }

    @PostMapping("/send")
    public Message sendMessage(@RequestBody Message message, @RequestHeader("Authorization") String authHeader) {
        String senderEmail = extractEmailFromHeader(authHeader);
        if (message.getReceiverEmail() == null || message.getReceiverEmail().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receiver email is required");
        }
        message.setSenderEmail(senderEmail);
        message.setSentAt(LocalDateTime.now());
        return messageService.sendMessage(message);
    }


    @GetMapping("/inbox")
    public List<Message> getInbox(@RequestHeader("Authorization") String authHeader) {
        String email = extractEmailFromHeader(authHeader);
        return messageService.getInbox(email);
    }



    @GetMapping("/sent")
    public List<Message> getSent(@RequestHeader("Authorization") String authHeader) {
        String email = extractEmailFromHeader(authHeader);
        return messageService.getSend(email);
    }

    @GetMapping("/starred")
    public List<Message> getStarred(@RequestHeader("Authorization") String authHeader) {
        String email = extractEmailFromHeader(authHeader);
        return messageService.getStarred(email);
    }

    @GetMapping("/drafts")
    public List<Message> getDrafts(@RequestHeader("Authorization") String authHeader) {
        String email = extractEmailFromHeader(authHeader);
        return messageService.getDrafts(email);
    }

    @GetMapping("/trash")
    public List<Message> getTrash(@RequestHeader("Authorization") String authHeader) {
        String email = extractEmailFromHeader(authHeader);
        return messageService.getTrash(email);
    }

    @PutMapping("/star/{id}")
    public Message toggleStar(@PathVariable String id,@RequestHeader("Authorization") String authHeader) {
        String email=extractEmailFromHeader(authHeader);
        Message message=messageService.getMessageById(id);
        if(!message.getReceiverEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return messageService.toggleStart(id);
    }
}

