package com.queueless.dto;

import com.queueless.entity.Organization;
import com.queueless.entity.OrganizationType;

import java.time.Instant;

public class OrganizationDto {
    private Long id;
    private String name;
    private String code;
    private OrganizationType type;
    private String description;
    private String address;
    private Boolean active;
    private Instant createdAt;
    private int departmentCount;
    private int activeQueuesCount;

    public OrganizationDto() {}

    public static OrganizationDto fromEntity(Organization org) {
        if (org == null) return null;
        OrganizationDto dto = new OrganizationDto();
        dto.setId(org.getId());
        dto.setName(org.getName());
        dto.setCode(org.getCode());
        dto.setType(org.getType());
        dto.setDescription(org.getDescription());
        dto.setAddress(org.getAddress());
        dto.setActive(org.getActive());
        dto.setCreatedAt(org.getCreatedAt());
        if (org.getDepartments() != null) {
            dto.setDepartmentCount(org.getDepartments().size());
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public OrganizationType getType() { return type; }
    public void setType(OrganizationType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public int getDepartmentCount() { return departmentCount; }
    public void setDepartmentCount(int departmentCount) { this.departmentCount = departmentCount; }

    public int getActiveQueuesCount() { return activeQueuesCount; }
    public void setActiveQueuesCount(int activeQueuesCount) { this.activeQueuesCount = activeQueuesCount; }
}
