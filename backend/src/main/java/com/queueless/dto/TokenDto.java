package com.queueless.dto;

import com.queueless.entity.QueueToken;
import com.queueless.entity.TokenPriority;
import com.queueless.entity.TokenStatus;

import java.time.Instant;

public class TokenDto {
    private Long id;
    private String tokenNumber;
    private Long organizationId;
    private String organizationName;
    private Long departmentId;
    private String departmentName;
    private Long serviceId;
    private String serviceName;
    private Long counterId;
    private String counterName;
    private String counterNumber;
    private Long customerUserId;
    private String customerName;
    private String customerPhone;
    private TokenPriority priority;
    private TokenStatus status;
    private Integer sequenceNumber;
    private Instant createdAt;
    private Instant calledAt;
    private Instant servedAt;
    private Instant completedAt;
    private Integer estimatedWaitMinutes;
    private Integer recallCount;
    private Long peopleAhead;

    public TokenDto() {}

    public static TokenDto fromEntity(QueueToken token) {
        if (token == null) return null;
        TokenDto dto = new TokenDto();
        dto.setId(token.getId());
        dto.setTokenNumber(token.getTokenNumber());
        if (token.getOrganization() != null) {
            dto.setOrganizationId(token.getOrganization().getId());
            dto.setOrganizationName(token.getOrganization().getName());
        }
        if (token.getDepartment() != null) {
            dto.setDepartmentId(token.getDepartment().getId());
            dto.setDepartmentName(token.getDepartment().getName());
        }
        if (token.getService() != null) {
            dto.setServiceId(token.getService().getId());
            dto.setServiceName(token.getService().getName());
        }
        if (token.getServiceCounter() != null) {
            dto.setCounterId(token.getServiceCounter().getId());
            dto.setCounterName(token.getServiceCounter().getName());
            dto.setCounterNumber(token.getServiceCounter().getCounterNumber());
        }
        if (token.getCustomerUser() != null) {
            dto.setCustomerUserId(token.getCustomerUser().getId());
        }
        dto.setCustomerName(token.getCustomerName());
        dto.setCustomerPhone(token.getCustomerPhone());
        dto.setPriority(token.getPriority());
        dto.setStatus(token.getStatus());
        dto.setSequenceNumber(token.getSequenceNumber());
        dto.setCreatedAt(token.getCreatedAt());
        dto.setCalledAt(token.getCalledAt());
        dto.setServedAt(token.getServedAt());
        dto.setCompletedAt(token.getCompletedAt());
        dto.setEstimatedWaitMinutes(token.getEstimatedWaitMinutes());
        dto.setRecallCount(token.getRecallCount());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public Long getCounterId() { return counterId; }
    public void setCounterId(Long counterId) { this.counterId = counterId; }

    public String getCounterName() { return counterName; }
    public void setCounterName(String counterName) { this.counterName = counterName; }

    public String getCounterNumber() { return counterNumber; }
    public void setCounterNumber(String counterNumber) { this.counterNumber = counterNumber; }

    public Long getCustomerUserId() { return customerUserId; }
    public void setCustomerUserId(Long customerUserId) { this.customerUserId = customerUserId; }

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

    public Long getPeopleAhead() { return peopleAhead; }
    public void setPeopleAhead(Long peopleAhead) { this.peopleAhead = peopleAhead; }
}
