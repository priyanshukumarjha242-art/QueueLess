package com.queueless.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "service_counters")
public class ServiceCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false, length = 60)
    private String counterNumber;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_staff_id")
    private User currentStaff;

    @Column(nullable = false)
    private Boolean active = true;

    public ServiceCounter() {}

    public ServiceCounter(Department department, String counterNumber, String name) {
        this.department = department;
        this.counterNumber = counterNumber;
        this.name = name;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getCounterNumber() { return counterNumber; }
    public void setCounterNumber(String counterNumber) { this.counterNumber = counterNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public User getCurrentStaff() { return currentStaff; }
    public void setCurrentStaff(User currentStaff) { this.currentStaff = currentStaff; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
