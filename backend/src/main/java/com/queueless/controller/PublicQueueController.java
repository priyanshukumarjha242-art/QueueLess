package com.queueless.controller;

import com.queueless.dto.*;
import com.queueless.service.DepartmentService;
import com.queueless.service.OrganizationService;
import com.queueless.service.QueueEngineService;
import com.queueless.service.ServiceCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicQueueController {

    private final OrganizationService organizationService;
    private final DepartmentService departmentService;
    private final ServiceCatalogService serviceCatalogService;
    private final QueueEngineService queueEngineService;

    public PublicQueueController(OrganizationService organizationService,
                                 DepartmentService departmentService,
                                 ServiceCatalogService serviceCatalogService,
                                 QueueEngineService queueEngineService) {
        this.organizationService = organizationService;
        this.departmentService = departmentService;
        this.serviceCatalogService = serviceCatalogService;
        this.queueEngineService = queueEngineService;
    }

    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationDto>> getOrganizations() {
        return ResponseEntity.ok(organizationService.getActiveOrganizations());
    }

    @GetMapping("/organizations/{id}")
    public ResponseEntity<OrganizationDto> getOrganization(@PathVariable Long id) {
        return ResponseEntity.ok(organizationService.getOrganizationById(id));
    }

    @GetMapping("/organizations/{orgId}/departments")
    public ResponseEntity<List<DepartmentDto>> getDepartments(@PathVariable Long orgId) {
        return ResponseEntity.ok(departmentService.getDepartmentsByOrganization(orgId));
    }

    @GetMapping("/departments/{deptId}/services")
    public ResponseEntity<List<ServiceDto>> getServicesByDepartment(@PathVariable Long deptId) {
        return ResponseEntity.ok(serviceCatalogService.getServicesByDepartment(deptId));
    }

    @GetMapping("/organizations/{orgId}/services")
    public ResponseEntity<List<ServiceDto>> getServicesByOrg(@PathVariable Long orgId) {
        return ResponseEntity.ok(serviceCatalogService.getServicesByOrganization(orgId));
    }

    @GetMapping("/board/{organizationId}")
    public ResponseEntity<PublicQueueBoardDto> getPublicBoard(@PathVariable Long organizationId) {
        return ResponseEntity.ok(queueEngineService.getPublicQueueBoard(organizationId));
    }

    @GetMapping("/tokens/{tokenId}")
    public ResponseEntity<TokenDto> getPublicTokenTracker(@PathVariable Long tokenId) {
        return ResponseEntity.ok(queueEngineService.getTokenById(tokenId));
    }
}
