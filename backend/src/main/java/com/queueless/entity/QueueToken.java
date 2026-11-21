package com.queueless.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "queue_tokens", indexes = {
    @Index(name = "idx_token_service_status", columnList = "service_id, status"),
    @Index(name = "idx_token_customer", columnList = "customer_user_id"),
    @Index(name = "idx_token_counter", columnList = "counter_id")
})
public class QueueToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String tokenNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counter_id")
    private ServiceCounter serviceCounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_user_id")
    private User customerUser;

    @Column(nullable = false, length = 100)
    private String customerName;

    @Column(length = 25)
    private String customerPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TokenPriority priority = TokenPriority.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TokenStatus status = TokenStatus.WAITING;

    @Column(nullable = false)
    private Integer sequenceNumber;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant calledAt;
    private Instant servedAt;
    private Instant completedAt;

    @Column(nullable = false)
    private Integer estimatedWaitMinutes = 0;

    @Column(nullable = false)
    private Integer recallCount = 0;

    public QueueToken() {}

    public QueueToken(String tokenNumber, Organization organization, Department department,
                      ServiceEntity service, User customerUser, String customerName,
                      String customerPhone, TokenPriority priority, Integer sequenceNumber,
                      Integer estimatedWaitMinutes) {
        this.tokenNumber = tokenNumber;
        this.organization = organization;
        this.department = department;
        this.service = service;
        this.customerUser = customerUser;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.priority = priority != null ? priority : TokenPriority.REGULAR;
        this.sequenceNumber = sequenceNumber;
        this.status = TokenStatus.WAITING;
        this.createdAt = Instant.now();
        this.estimatedWaitMinutes = estimatedWaitMinutes != null ? estimatedWaitMinutes : 0;
        this.recallCount = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public ServiceEntity getService() { return service; }
    public void setService(ServiceEntity service) { this.service = service; }

    public ServiceCounter getServiceCounter() { return serviceCounter; }
    public void setServiceCounter(ServiceCounter serviceCounter) { this.serviceCounter = serviceCounter; }

    public User getCustomerUser() { return customerUser; }
    public void setCustomerUser(User customerUser) { this.customerUser = customerUser; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public TokenPriority getPriority() { return priority; }
    public void setPriority(TokenPriority priority) { this.priority = priority; }

    public TokenStatus getStatus() { return status; }
    public void setStatus(TokenStatus status) { this.status = status; }

    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getCalledAt() { return calledAt; }
    public void setCalledAt(Instant calledAt) { this.calledAt = calledAt; }

    public Instant getServedAt() { return servedAt; }
    public void setServedAt(Instant servedAt) { this.servedAt = servedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Integer getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public void setEstimatedWaitMinutes(Integer estimatedWaitMinutes) { this.estimatedWaitMinutes = estimatedWaitMinutes; }

    public Integer getRecallCount() { return recallCount; }
    public void setRecallCount(Integer recallCount) { this.recallCount = recallCount; }
}
