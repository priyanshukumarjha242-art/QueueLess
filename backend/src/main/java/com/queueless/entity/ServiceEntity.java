package com.queueless.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "services")
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 10)
    private String prefix;

    @Column(nullable = false)
    private Integer estimatedDurationMinutes = 10;

    @Column(nullable = false)
    private Integer priorityLevel = 1; // 1 = standard, 2 = elevated

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    public ServiceEntity() {}

    public ServiceEntity(Department department, String name, String code, String prefix, Integer estimatedDurationMinutes, Integer priorityLevel, String description) {
        this.department = department;
        this.name = name;
        this.code = code;
        this.prefix = prefix;
        this.estimatedDurationMinutes = estimatedDurationMinutes != null ? estimatedDurationMinutes : 10;
        this.priorityLevel = priorityLevel != null ? priorityLevel : 1;
        this.description = description;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
