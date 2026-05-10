package br.com.ctkd.mail.controller;

import br.com.ctkd.mail.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/welcome")
    public ResponseEntity<String> sendWelcome(@RequestBody EmailRequest request) {
        emailService.sendWelcome(request.destination(), request.name(), request.accessLink());
        return ResponseEntity.ok("Email sent to: " + request.destination());
    }
}
