package com.queueless.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "staff_assignments")
public class StaffAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "counter_id", nullable = false)
    private ServiceCounter counter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    @Column(nullable = false, updatable = false)
    private Instant assignedAt = Instant.now();

    @Column(nullable = false)
    private Boolean active = true;

    public StaffAssignment() {}

    public StaffAssignment(User staff, ServiceCounter counter, ServiceEntity service) {
        this.staff = staff;
        this.counter = counter;
        this.service = service;
        this.assignedAt = Instant.now();
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStaff() { return staff; }
    public void setStaff(User staff) { this.staff = staff; }

    public ServiceCounter getCounter() { return counter; }
    public void setCounter(ServiceCounter counter) { this.counter = counter; }

    public ServiceEntity getService() { return service; }
    public void setService(ServiceEntity service) { this.service = service; }

    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
