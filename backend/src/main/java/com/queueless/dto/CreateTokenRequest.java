package com.queueless.dto;

import com.queueless.entity.TokenPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateTokenRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String customerPhone;

    private TokenPriority priority = TokenPriority.REGULAR;

    public CreateTokenRequest() {}

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public TokenPriority getPriority() { return priority; }
    public void setPriority(TokenPriority priority) { this.priority = priority; }
}
