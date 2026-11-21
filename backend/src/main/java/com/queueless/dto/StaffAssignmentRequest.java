package com.queueless.dto;

import jakarta.validation.constraints.NotNull;

public class StaffAssignmentRequest {
    @NotNull(message = "Staff ID is required")
    private Long staffId;

    @NotNull(message = "Counter ID is required")
    private Long counterId;

    private Long serviceId;

    public StaffAssignmentRequest() {}

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public Long getCounterId() { return counterId; }
    public void setCounterId(Long counterId) { this.counterId = counterId; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
}
