package com.queueless.controller;

import com.queueless.dto.CounterDto;
import com.queueless.dto.StaffAssignmentRequest;
import com.queueless.service.CounterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/counters")
public class CounterController {

    private final CounterService counterService;

    public CounterController(CounterService counterService) {
        this.counterService = counterService;
    }

    @GetMapping("/org/{orgId}")
    public ResponseEntity<List<CounterDto>> getCountersByOrg(@PathVariable Long orgId) {
        return ResponseEntity.ok(counterService.getCountersByOrganization(orgId));
    }

    @GetMapping("/dept/{deptId}")
    public ResponseEntity<List<CounterDto>> getCountersByDept(@PathVariable Long deptId) {
        return ResponseEntity.ok(counterService.getCountersByDepartment(deptId));
    }

    @PostMapping
    public ResponseEntity<CounterDto> createCounter(@Valid @RequestBody CounterDto dto) {
        return ResponseEntity.ok(counterService.createCounter(dto));
    }

    @PostMapping("/assign")
    public ResponseEntity<CounterDto> assignStaff(@Valid @RequestBody StaffAssignmentRequest req) {
        return ResponseEntity.ok(counterService.assignStaffToCounter(req));
    }

    @PostMapping("/{counterId}/unassign")
    public ResponseEntity<CounterDto> unassignStaff(@PathVariable Long counterId) {
        return ResponseEntity.ok(counterService.unassignStaffFromCounter(counterId));
    }
}
