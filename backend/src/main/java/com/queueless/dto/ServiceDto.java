package com.queueless.dto;

import com.queueless.entity.ServiceEntity;

public class ServiceDto {
    private Long id;
    private Long departmentId;
    private String departmentName;
    private Long organizationId;
    private String organizationName;
    private String name;
    private String code;
    private String prefix;
    private Integer estimatedDurationMinutes;
    private Integer priorityLevel;
    private String description;
    private Boolean active;
    private long currentWaitingCount;

    public ServiceDto() {}

    public static ServiceDto fromEntity(ServiceEntity service) {
        if (service == null) return null;
        ServiceDto dto = new ServiceDto();
        dto.setId(service.getId());
        if (service.getDepartment() != null) {
            dto.setDepartmentId(service.getDepartment().getId());
            dto.setDepartmentName(service.getDepartment().getName());
            if (service.getDepartment().getOrganization() != null) {
                dto.setOrganizationId(service.getDepartment().getOrganization().getId());
                dto.setOrganizationName(service.getDepartment().getOrganization().getName());
            }
        }
        dto.setName(service.getName());
        dto.setCode(service.getCode());
        dto.setPrefix(service.getPrefix());
        dto.setEstimatedDurationMinutes(service.getEstimatedDurationMinutes());
        dto.setPriorityLevel(service.getPriorityLevel());
        dto.setDescription(service.getDescription());
        dto.setActive(service.getActive());
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

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

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

    public long getCurrentWaitingCount() { return currentWaitingCount; }
    public void setCurrentWaitingCount(long currentWaitingCount) { this.currentWaitingCount = currentWaitingCount; }
}
