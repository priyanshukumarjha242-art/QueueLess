package com.queueless.dto;

import com.queueless.entity.Department;

public class DepartmentDto {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private String name;
    private String code;
    private String description;
    private Boolean active;
    private int serviceCount;
    private int counterCount;

    public DepartmentDto() {}

    public static DepartmentDto fromEntity(Department dept) {
        if (dept == null) return null;
        DepartmentDto dto = new DepartmentDto();
        dto.setId(dept.getId());
        if (dept.getOrganization() != null) {
            dto.setOrganizationId(dept.getOrganization().getId());
            dto.setOrganizationName(dept.getOrganization().getName());
        }
        dto.setName(dept.getName());
        dto.setCode(dept.getCode());
        dto.setDescription(dept.getDescription());
        dto.setActive(dept.getActive());
        if (dept.getServices() != null) {
            dto.setServiceCount(dept.getServices().size());
        }
        if (dept.getCounters() != null) {
            dto.setCounterCount(dept.getCounters().size());
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public int getServiceCount() { return serviceCount; }
    public void setServiceCount(int serviceCount) { this.serviceCount = serviceCount; }

    public int getCounterCount() { return counterCount; }
    public void setCounterCount(int counterCount) { this.counterCount = counterCount; }
}
