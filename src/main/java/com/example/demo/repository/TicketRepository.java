package com.example.demo.repository;

import com.example.demo.model.Ticket;
import com.example.demo.model.TicketStatus;
import com.example.demo.model.Priority;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomer(User customer);
    List<Ticket> findByAssignedAgent(User agent);
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByPriority(Priority priority);
    List<Ticket> findByStatusAndPriority(TicketStatus status, Priority priority);
    List<Ticket> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Ticket> findByAssignedAgentOrderByCreatedAtDesc(User agent);
    List<Ticket> findAllByOrderByCreatedAtDesc();
    long countByStatus(TicketStatus status);
    long countByPriority(Priority priority);
}
