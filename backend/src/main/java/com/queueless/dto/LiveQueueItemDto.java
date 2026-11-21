package com.queueless.dto;

import com.queueless.entity.TokenPriority;
import com.queueless.entity.TokenStatus;

public class LiveQueueItemDto {
    private Long id;
    private String tokenNumber;
    private TokenPriority priority;
    private TokenStatus status;
    private String serviceName;
    private String counterNumber;
    private String counterName;
    private String customerName;
    private Integer estimatedWaitMinutes;
    private Long peopleAhead;

    public LiveQueueItemDto() {}

    public LiveQueueItemDto(Long id, String tokenNumber, TokenPriority priority, TokenStatus status,
                            String serviceName, String counterNumber, String counterName,
                            String customerName, Integer estimatedWaitMinutes, Long peopleAhead) {
        this.id = id;
        this.tokenNumber = tokenNumber;
        this.priority = priority;
        this.status = status;
        this.serviceName = serviceName;
        this.counterNumber = counterNumber;
        this.counterName = counterName;
        this.customerName = customerName;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
        this.peopleAhead = peopleAhead;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

    public TokenPriority getPriority() { return priority; }
    public void setPriority(TokenPriority priority) { this.priority = priority; }

    public TokenStatus getStatus() { return status; }
    public void setStatus(TokenStatus status) { this.status = status; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getCounterNumber() { return counterNumber; }
    public void setCounterNumber(String counterNumber) { this.counterNumber = counterNumber; }

    public String getCounterName() { return counterName; }
    public void setCounterName(String counterName) { this.counterName = counterName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Integer getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public void setEstimatedWaitMinutes(Integer estimatedWaitMinutes) { this.estimatedWaitMinutes = estimatedWaitMinutes; }

    public Long getPeopleAhead() { return peopleAhead; }
    public void setPeopleAhead(Long peopleAhead) { this.peopleAhead = peopleAhead; }
}
