package com.queueless.controller;

import com.queueless.dto.AnalyticsSummaryDto;
import com.queueless.dto.RegisterRequest;
import com.queueless.dto.UserDto;
import com.queueless.service.StatisticsService;
import com.queueless.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminAnalyticsController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    public AdminAnalyticsController(StatisticsService statisticsService, UserService userService) {
        this.statisticsService = statisticsService;
        this.userService = userService;
    }

    @GetMapping("/analytics/{orgId}")
    public ResponseEntity<AnalyticsSummaryDto> getAnalytics(@PathVariable Long orgId,
                                                            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(statisticsService.getAnalytics(orgId, days));
    }

    @GetMapping("/staff/{orgId}")
    public ResponseEntity<List<UserDto>> getStaffMembers(@PathVariable Long orgId) {
        return ResponseEntity.ok(userService.getStaffByOrganization(orgId));
    }

    @PostMapping("/staff")
    public ResponseEntity<UserDto> createStaffMember(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(userService.createStaffUser(req));
    }

    @PatchMapping("/users/{userId}/toggle-status")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.toggleUserStatus(userId));
    }
}
