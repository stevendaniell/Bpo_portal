package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.TicketRequest;
import com.example.demo.dto.TicketResponse;
import com.example.demo.model.Priority;
import com.example.demo.model.Role;
import com.example.demo.model.Ticket;
import com.example.demo.model.TicketStatus;
import com.example.demo.model.User;
import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    // Customer creates a ticket
    @PostMapping
    public ResponseEntity<?> createTicket(@Valid @RequestBody TicketRequest request,
                                          Authentication authentication) {
        User customer = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Priority priority = Priority.MEDIUM;
        if (request.getPriority() != null) {
            try {
                priority = Priority.valueOf(request.getPriority().toUpperCase());
            } catch (IllegalArgumentException e) {
                priority = Priority.MEDIUM;
            }
        }

        Ticket ticket = new Ticket(request.getTitle(), request.getDescription(), priority, customer);
        ticketRepository.save(ticket);

        return ResponseEntity.ok(Map.of("message", "Ticket created successfully",
                "ticket", TicketResponse.fromTicket(ticket)));
    }

    // Get tickets for the logged-in user (customers see their own, agents/admin see assigned or all)
    @GetMapping
    public ResponseEntity<?> getTickets(Authentication authentication,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String priority) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Ticket> tickets;

        if (user.getRole() == Role.CUSTOMER) {
            tickets = ticketRepository.findByCustomerOrderByCreatedAtDesc(user);
        } else {
            tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        }

        // Apply filters
        if (status != null && !status.isEmpty()) {
            TicketStatus ts = TicketStatus.valueOf(status.toUpperCase());
            tickets = tickets.stream().filter(t -> t.getStatus() == ts).collect(Collectors.toList());
        }
        if (priority != null && !priority.isEmpty()) {
            Priority p = Priority.valueOf(priority.toUpperCase());
            tickets = tickets.stream().filter(t -> t.getPriority() == p).collect(Collectors.toList());
        }

        List<TicketResponse> response = tickets.stream()
                .map(TicketResponse::fromTicket)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Get single ticket by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getTicket(@PathVariable Long id, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Customers can only view their own tickets
        if (user.getRole() == Role.CUSTOMER && !ticket.getCustomer().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        return ResponseEntity.ok(TicketResponse.fromTicket(ticket));
    }

    // Update ticket status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTicketStatus(@PathVariable Long id,
                                                @RequestBody Map<String, String> body,
                                                Authentication authentication) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        String newStatus = body.get("status");
        if (newStatus != null) {
            ticket.setStatus(TicketStatus.valueOf(newStatus.toUpperCase()));
        }

        String notes = body.get("resolutionNotes");
        if (notes != null) {
            ticket.setResolutionNotes(notes);
        }

        ticketRepository.save(ticket);
        return ResponseEntity.ok(Map.of("message", "Ticket updated", "ticket", TicketResponse.fromTicket(ticket)));
    }
}
