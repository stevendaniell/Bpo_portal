package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.TicketResponse;
import com.example.demo.model.Priority;
import com.example.demo.model.Role;
import com.example.demo.model.Ticket;
import com.example.demo.model.TicketStatus;
import com.example.demo.model.User;
import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
public class AdminController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    // Dashboard stats
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTickets", ticketRepository.count());
        stats.put("openTickets", ticketRepository.countByStatus(TicketStatus.OPEN));
        stats.put("pendingTickets", ticketRepository.countByStatus(TicketStatus.PENDING));
        stats.put("resolvedTickets", ticketRepository.countByStatus(TicketStatus.RESOLVED));
        stats.put("highPriority", ticketRepository.countByPriority(Priority.HIGH));
        stats.put("mediumPriority", ticketRepository.countByPriority(Priority.MEDIUM));
        stats.put("lowPriority", ticketRepository.countByPriority(Priority.LOW));
        stats.put("totalAgents", userRepository.findByRole(Role.AGENT).size());
        stats.put("totalCustomers", userRepository.findByRole(Role.CUSTOMER).size());
        return ResponseEntity.ok(stats);
    }

    // Get all agents
    @GetMapping("/agents")
    public ResponseEntity<?> getAgents() {
        List<User> agents = userRepository.findByRole(Role.AGENT);
        List<Map<String, Object>> agentList = agents.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("username", a.getUsername());
            m.put("fullName", a.getFullName());
            m.put("email", a.getEmail());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(agentList);
    }

    // Assign ticket to agent
    @PutMapping("/tickets/{ticketId}/assign")
    public ResponseEntity<?> assignTicket(@PathVariable Long ticketId,
                                          @RequestBody Map<String, Long> body) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        Long agentId = body.get("agentId");
        if (agentId != null) {
            User agent = userRepository.findById(agentId)
                    .orElseThrow(() -> new RuntimeException("Agent not found"));
            ticket.setAssignedAgent(agent);
            ticket.setStatus(TicketStatus.PENDING);
        }

        ticketRepository.save(ticket);
        return ResponseEntity.ok(Map.of("message", "Ticket assigned successfully",
                "ticket", TicketResponse.fromTicket(ticket)));
    }

    // Filter tickets by priority
    @GetMapping("/tickets/filter")
    public ResponseEntity<?> filterTickets(@RequestParam(required = false) String priority,
                                           @RequestParam(required = false) String status) {
        List<Ticket> tickets;

        if (priority != null && status != null) {
            tickets = ticketRepository.findByStatusAndPriority(
                    TicketStatus.valueOf(status.toUpperCase()),
                    Priority.valueOf(priority.toUpperCase()));
        } else if (priority != null) {
            tickets = ticketRepository.findByPriority(Priority.valueOf(priority.toUpperCase()));
        } else if (status != null) {
            tickets = ticketRepository.findByStatus(TicketStatus.valueOf(status.toUpperCase()));
        } else {
            tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        }

        List<TicketResponse> response = tickets.stream()
                .map(TicketResponse::fromTicket)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
