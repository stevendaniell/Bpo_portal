package com.example.demo.dto;

import java.time.format.DateTimeFormatter;

import com.example.demo.model.Ticket;

public class TicketResponse {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String customerName;
    private String customerEmail;
    private String assignedAgentName;
    private Long assignedAgentId;
    private String resolutionNotes;
    private String createdAt;
    private String updatedAt;

    public static TicketResponse fromTicket(Ticket ticket) {
        TicketResponse r = new TicketResponse();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        r.id = ticket.getId();
        r.title = ticket.getTitle();
        r.description = ticket.getDescription();
        r.status = ticket.getStatus().name();
        r.priority = ticket.getPriority().name();
        r.customerName = ticket.getCustomer().getFullName();
        r.customerEmail = ticket.getCustomer().getEmail();
        r.assignedAgentName = ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getFullName() : "Unassigned";
        r.assignedAgentId = ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getId() : null;
        r.resolutionNotes = ticket.getResolutionNotes();
        r.createdAt = ticket.getCreatedAt() != null ? ticket.getCreatedAt().format(fmt) : "";
        r.updatedAt = ticket.getUpdatedAt() != null ? ticket.getUpdatedAt().format(fmt) : "";
        return r;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getAssignedAgentName() { return assignedAgentName; }
    public Long getAssignedAgentId() { return assignedAgentId; }
    public String getResolutionNotes() { return resolutionNotes; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
