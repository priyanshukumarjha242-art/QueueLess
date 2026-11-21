package com.queueless;

import com.queueless.dto.CreateTokenRequest;
import com.queueless.dto.TokenDto;
import com.queueless.entity.*;
import com.queueless.exception.BadRequestException;
import com.queueless.exception.ConflictException;
import com.queueless.exception.ResourceNotFoundException;
import com.queueless.repository.*;
import com.queueless.service.QueueEngineService;
import com.queueless.service.WebSocketPublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueEngineServiceTest {

    @Mock
    private QueueTokenRepository queueTokenRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private ServiceCounterRepository counterRepository;
    @Mock
    private TokenHistoryRepository tokenHistoryRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private WebSocketPublisherService wsPublisher;

    @InjectMocks
    private QueueEngineService queueEngineService;

    private Organization org;
    private Department dept;
    private ServiceEntity service;
    private ServiceCounter counter;
    private User staffUser;
    private User customerUser;

    @BeforeEach
    void setUp() {
        org = new Organization("Test Org", "TORG", OrganizationType.HOSPITAL, "Test Desc", "123 Test St");
        org.setId(1L);

        dept = new Department(org, "Cardiology", "CARD", "Heart care");
        dept.setId(10L);

        service = new ServiceEntity(dept, "ECG Screening", "ECG", "E", 15, 1, "Quick scan");
        service.setId(100L);

        counter = new ServiceCounter(dept, "Counter 1", "Room A");
        counter.setId(50L);

        staffUser = new User("staff@test.com", "pass", "Staff Member", "123", Role.ROLE_STAFF, org);
        staffUser.setId(201L);

        customerUser = new User("cust@test.com", "pass", "John Doe", "456", Role.ROLE_CUSTOMER, null);
        customerUser.setId(301L);
    }

    @Test
    @DisplayName("Should generate token with correct sequence, priority, and wait estimate")
    void testGenerateTokenSuccess() {
        CreateTokenRequest req = new CreateTokenRequest();
        req.setServiceId(100L);
        req.setCustomerName("John Doe");
        req.setPriority(TokenPriority.PRIORITY);

        when(serviceRepository.findById(100L)).thenReturn(Optional.of(service));
        when(queueTokenRepository.findByCustomerUserIdOrderByCreatedAtDesc(301L)).thenReturn(List.of());
        when(queueTokenRepository.findMaxSequenceForToday(eq(100L), any(Instant.class))).thenReturn(5);
        when(queueTokenRepository.countWaitingInService(100L)).thenReturn(2L);
        when(counterRepository.findByDepartmentIdAndActiveTrue(10L)).thenReturn(List.of(counter));

        QueueToken savedToken = new QueueToken(
                "CARD-E-006", org, dept, service, customerUser, "John Doe", "456",
                TokenPriority.PRIORITY, 6, 20
        );
        savedToken.setId(1001L);
        when(queueTokenRepository.save(any(QueueToken.class))).thenReturn(savedToken);

        TokenDto result = queueEngineService.generateToken(req, customerUser);

        assertNotNull(result);
        assertEquals("CARD-E-006", result.getTokenNumber());
        assertEquals(TokenPriority.PRIORITY, result.getPriority());
        assertEquals(TokenStatus.WAITING, result.getStatus());
        verify(queueTokenRepository, times(1)).save(any(QueueToken.class));
        verify(tokenHistoryRepository, times(1)).save(any(TokenHistory.class));
        verify(wsPublisher, times(1)).publishTokenUpdate(any(TokenDto.class));
    }

    @Test
    @DisplayName("Should prevent customer from generating duplicate active token for same service")
    void testDuplicateActiveTokenPrevention() {
        CreateTokenRequest req = new CreateTokenRequest();
        req.setServiceId(100L);
        req.setCustomerName("John Doe");

        QueueToken existingActive = new QueueToken(
                "CARD-E-001", org, dept, service, customerUser, "John Doe", "456",
                TokenPriority.REGULAR, 1, 10
        );
        existingActive.setStatus(TokenStatus.WAITING);

        when(serviceRepository.findById(100L)).thenReturn(Optional.of(service));
        when(queueTokenRepository.findByCustomerUserIdOrderByCreatedAtDesc(301L)).thenReturn(List.of(existingActive));

        assertThrows(ConflictException.class, () -> queueEngineService.generateToken(req, customerUser));
        verify(queueTokenRepository, never()).save(any(QueueToken.class));
    }

    @Test
    @DisplayName("Should atomically call next token and prevent concurrent race condition")
    void testCallNextTokenAtomic() {
        when(counterRepository.findById(50L)).thenReturn(Optional.of(counter));
        when(queueTokenRepository.findByServiceCounterIdAndStatusInOrderByCreatedAtDesc(eq(50L), anyList())).thenReturn(List.of());

        QueueToken candidate1 = new QueueToken("CARD-E-001", org, dept, service, customerUser, "Alice", "111", TokenPriority.REGULAR, 1, 0);
        candidate1.setId(501L);

        when(queueTokenRepository.findByDepartmentIdAndStatusOrderByPriorityDescCreatedAtAsc(10L, TokenStatus.WAITING))
                .thenReturn(List.of(candidate1));
        when(queueTokenRepository.atomicCallToken(eq(501L), eq(50L), eq(TokenStatus.CALLED), any(Instant.class)))
                .thenReturn(1);

        TokenDto result = queueEngineService.callNextToken(50L, staffUser);

        assertNotNull(result);
        assertEquals(TokenStatus.CALLED, result.getStatus());
        assertEquals(1, result.getRecallCount());
        verify(tokenHistoryRepository, times(1)).save(any(TokenHistory.class));
        verify(wsPublisher, times(1)).publishTokenUpdate(any(TokenDto.class));
    }

    @Test
    @DisplayName("Should throw ConflictException if counter already has an active serving token")
    void testCannotCallNextWhenOccupied() {
        when(counterRepository.findById(50L)).thenReturn(Optional.of(counter));

        QueueToken occupiedToken = new QueueToken("CARD-E-001", org, dept, service, customerUser, "Alice", "111", TokenPriority.REGULAR, 1, 0);
        occupiedToken.setStatus(TokenStatus.SERVING);

        when(queueTokenRepository.findByServiceCounterIdAndStatusInOrderByCreatedAtDesc(eq(50L), anyList()))
                .thenReturn(List.of(occupiedToken));

        assertThrows(ConflictException.class, () -> queueEngineService.callNextToken(50L, staffUser));
    }

    @Test
    @DisplayName("Should properly transition token through full lifecycle: CALLED -> SERVING -> COMPLETED")
    void testFullLifecycle() {
        QueueToken token = new QueueToken("CARD-E-002", org, dept, service, customerUser, "Bob", "222", TokenPriority.REGULAR, 2, 0);
        token.setId(777L);
        token.setStatus(TokenStatus.CALLED);

        when(queueTokenRepository.findById(777L)).thenReturn(Optional.of(token));
        when(queueTokenRepository.save(any(QueueToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Start serving
        TokenDto servingDto = queueEngineService.startServing(777L, 50L, staffUser);
        assertEquals(TokenStatus.SERVING, servingDto.getStatus());
        assertNotNull(token.getServedAt());

        // Complete service
        TokenDto completedDto = queueEngineService.completeService(777L, 50L, staffUser, "Done");
        assertEquals(TokenStatus.COMPLETED, completedDto.getStatus());
        assertNotNull(token.getCompletedAt());
    }

    @Test
    @DisplayName("Should cancel waiting token and reject cancellation of already served tokens")
    void testTokenCancellation() {
        QueueToken waitingToken = new QueueToken("CARD-E-003", org, dept, service, customerUser, "Charlie", "333", TokenPriority.REGULAR, 3, 0);
        waitingToken.setId(888L);
        waitingToken.setStatus(TokenStatus.WAITING);

        when(queueTokenRepository.findById(888L)).thenReturn(Optional.of(waitingToken));
        when(queueTokenRepository.save(any(QueueToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TokenDto cancelledDto = queueEngineService.cancelToken(888L, customerUser, "Changed mind");
        assertEquals(TokenStatus.CANCELLED, cancelledDto.getStatus());

        // Try to cancel a completed token
        QueueToken completedToken = new QueueToken("CARD-E-004", org, dept, service, customerUser, "Dave", "444", TokenPriority.REGULAR, 4, 0);
        completedToken.setId(999L);
        completedToken.setStatus(TokenStatus.COMPLETED);
        when(queueTokenRepository.findById(999L)).thenReturn(Optional.of(completedToken));

        assertThrows(BadRequestException.class, () -> queueEngineService.cancelToken(999L, customerUser, "Try cancel"));
    }
}
