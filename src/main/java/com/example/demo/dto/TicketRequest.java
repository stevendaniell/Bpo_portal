package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class TicketRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private String priority; // HIGH, MEDIUM, LOW

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
