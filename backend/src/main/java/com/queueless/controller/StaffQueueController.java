package com.queueless.controller;

import com.queueless.dto.CounterDto;
import com.queueless.dto.TokenActionRequest;
import com.queueless.dto.TokenDto;
import com.queueless.entity.User;
import com.queueless.exception.UnauthorizedException;
import com.queueless.service.AuthService;
import com.queueless.service.CounterService;
import com.queueless.service.QueueEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffQueueController {

    private final QueueEngineService queueEngineService;
    private final CounterService counterService;
    private final AuthService authService;

    public StaffQueueController(QueueEngineService queueEngineService,
                                CounterService counterService,
                                AuthService authService) {
        this.queueEngineService = queueEngineService;
        this.counterService = counterService;
        this.authService = authService;
    }

    @GetMapping("/my-counter")
    public ResponseEntity<CounterDto> getMyCounter() {
        User staff = getAuthenticatedStaff();
        return ResponseEntity.ok(counterService.getCounterByStaffUserId(staff.getId()));
    }

    @GetMapping("/waiting-tokens")
    public ResponseEntity<List<TokenDto>> getWaitingTokens(@RequestParam Long departmentId) {
        return ResponseEntity.ok(queueEngineService.getWaitingTokensForDepartment(departmentId));
    }

    @GetMapping("/active-token")
    public ResponseEntity<TokenDto> getActiveToken(@RequestParam Long counterId) {
        return ResponseEntity.ok(queueEngineService.getActiveTokenForCounter(counterId));
    }

    @GetMapping("/completed-tokens")
    public ResponseEntity<List<TokenDto>> getCompletedTokens(@RequestParam Long counterId) {
        return ResponseEntity.ok(queueEngineService.getCompletedTokensForCounter(counterId));
    }

    @PostMapping("/call-next")
    public ResponseEntity<TokenDto> callNext(@RequestParam Long counterId) {
        User staff = getAuthenticatedStaff();
        return ResponseEntity.ok(queueEngineService.callNextToken(counterId, staff));
    }

    @PostMapping("/tokens/{id}/recall")
    public ResponseEntity<TokenDto> recallToken(@PathVariable Long id, @RequestParam Long counterId) {
        User staff = getAuthenticatedStaff();
        return ResponseEntity.ok(queueEngineService.recallToken(id, counterId, staff));
    }

    @PostMapping("/tokens/{id}/start-serving")
    public ResponseEntity<TokenDto> startServing(@PathVariable Long id, @RequestParam Long counterId) {
        User staff = getAuthenticatedStaff();
        return ResponseEntity.ok(queueEngineService.startServing(id, counterId, staff));
    }

    @PostMapping("/tokens/{id}/complete")
    public ResponseEntity<TokenDto> completeService(@PathVariable Long id,
                                                    @RequestParam Long counterId,
                                                    @RequestBody(required = false) TokenActionRequest actionReq) {
        User staff = getAuthenticatedStaff();
        String notes = actionReq != null ? actionReq.getNotes() : null;
        return ResponseEntity.ok(queueEngineService.completeService(id, counterId, staff, notes));
    }

    @PostMapping("/tokens/{id}/no-show")
    public ResponseEntity<TokenDto> markNoShow(@PathVariable Long id,
                                               @RequestParam Long counterId,
                                               @RequestBody(required = false) TokenActionRequest actionReq) {
        User staff = getAuthenticatedStaff();
        String notes = actionReq != null ? actionReq.getNotes() : null;
        return ResponseEntity.ok(queueEngineService.markNoShow(id, counterId, staff, notes));
    }

    private User getAuthenticatedStaff() {
        User staff = authService.getCurrentAuthenticatedUser();
        if (staff == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return staff;
    }
}
