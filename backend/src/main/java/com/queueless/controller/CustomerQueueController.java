package com.queueless.controller;

import com.queueless.dto.CreateTokenRequest;
import com.queueless.dto.TokenActionRequest;
import com.queueless.dto.TokenDto;
import com.queueless.entity.User;
import com.queueless.exception.UnauthorizedException;
import com.queueless.service.AuthService;
import com.queueless.service.QueueEngineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerQueueController {

    private final QueueEngineService queueEngineService;
    private final AuthService authService;

    public CustomerQueueController(QueueEngineService queueEngineService, AuthService authService) {
        this.queueEngineService = queueEngineService;
        this.authService = authService;
    }

    @PostMapping("/tokens")
    public ResponseEntity<TokenDto> generateToken(@Valid @RequestBody CreateTokenRequest req) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        TokenDto token = queueEngineService.generateToken(req, currentUser);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/tokens")
    public ResponseEntity<List<TokenDto>> getMyTokens() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication required to view personal tokens");
        }
        return ResponseEntity.ok(queueEngineService.getCustomerTokens(currentUser.getId()));
    }

    @GetMapping("/tokens/{id}")
    public ResponseEntity<TokenDto> getTokenById(@PathVariable Long id) {
        return ResponseEntity.ok(queueEngineService.getTokenById(id));
    }

    @PostMapping("/tokens/{id}/cancel")
    public ResponseEntity<TokenDto> cancelToken(@PathVariable Long id, @RequestBody(required = false) TokenActionRequest actionReq) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        String notes = actionReq != null ? actionReq.getNotes() : "Cancelled by customer";
        return ResponseEntity.ok(queueEngineService.cancelToken(id, currentUser, notes));
    }
}
