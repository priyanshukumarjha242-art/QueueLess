package com.queueless.dto;

import com.queueless.entity.ServiceCounter;

public class CounterDto {
    private Long id;
    private Long departmentId;
    private String departmentName;
    private Long organizationId;
    private String counterNumber;
    private String name;
    private Long currentStaffId;
    private String currentStaffName;
    private Boolean active;
    private String currentServingToken;

    public CounterDto() {}

    public static CounterDto fromEntity(ServiceCounter counter) {
        if (counter == null) return null;
        CounterDto dto = new CounterDto();
        dto.setId(counter.getId());
        if (counter.getDepartment() != null) {
            dto.setDepartmentId(counter.getDepartment().getId());
            dto.setDepartmentName(counter.getDepartment().getName());
            if (counter.getDepartment().getOrganization() != null) {
                dto.setOrganizationId(counter.getDepartment().getOrganization().getId());
            }
        }
        dto.setCounterNumber(counter.getCounterNumber());
        dto.setName(counter.getName());
        if (counter.getCurrentStaff() != null) {
            dto.setCurrentStaffId(counter.getCurrentStaff().getId());
            dto.setCurrentStaffName(counter.getCurrentStaff().getFullName());
        }
        dto.setActive(counter.getActive());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getCounterNumber() { return counterNumber; }
    public void setCounterNumber(String counterNumber) { this.counterNumber = counterNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getCurrentStaffId() { return currentStaffId; }
    public void setCurrentStaffId(Long currentStaffId) { this.currentStaffId = currentStaffId; }

    public String getCurrentStaffName() { return currentStaffName; }
    public void setCurrentStaffName(String currentStaffName) { this.currentStaffName = currentStaffName; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getCurrentServingToken() { return currentServingToken; }
    public void setCurrentServingToken(String currentServingToken) { this.currentServingToken = currentServingToken; }
}
