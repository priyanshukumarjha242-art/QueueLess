package com.queueless.service;

import com.queueless.dto.CreateTokenRequest;
import com.queueless.dto.LiveQueueItemDto;
import com.queueless.dto.PublicQueueBoardDto;
import com.queueless.dto.TokenDto;
import com.queueless.entity.*;
import com.queueless.exception.BadRequestException;
import com.queueless.exception.ConflictException;
import com.queueless.exception.ResourceNotFoundException;
import com.queueless.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueueEngineService {

    private static final Logger log = LoggerFactory.getLogger(QueueEngineService.class);

    private final QueueTokenRepository queueTokenRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceCounterRepository counterRepository;
    private final TokenHistoryRepository tokenHistoryRepository;
    private final OrganizationRepository organizationRepository;
    private final WebSocketPublisherService wsPublisher;

    public QueueEngineService(QueueTokenRepository queueTokenRepository,
                              ServiceRepository serviceRepository,
                              ServiceCounterRepository counterRepository,
                              TokenHistoryRepository tokenHistoryRepository,
                              OrganizationRepository organizationRepository,
                              WebSocketPublisherService wsPublisher) {
        this.queueTokenRepository = queueTokenRepository;
        this.serviceRepository = serviceRepository;
        this.counterRepository = counterRepository;
        this.tokenHistoryRepository = tokenHistoryRepository;
        this.organizationRepository = organizationRepository;
        this.wsPublisher = wsPublisher;
    }

    /**
     * Customer or Walk-in Token Generation with Duplicate Protection & Dynamic Wait Estimation.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public synchronized TokenDto generateToken(CreateTokenRequest req, User customerUser) {
        ServiceEntity service = serviceRepository.findById(req.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + req.getServiceId()));

        if (!Boolean.TRUE.equals(service.getActive())) {
            throw new BadRequestException("This service is currently inactive");
        }

        Department department = service.getDepartment();
        Organization organization = department.getOrganization();

        // Prevent customer from spamming duplicate active tokens for the same service
        if (customerUser != null) {
            var existingActive = queueTokenRepository.findByCustomerUserIdOrderByCreatedAtDesc(customerUser.getId())
                    .stream()
                    .filter(t -> t.getService().getId().equals(service.getId()) &&
                            (t.getStatus() == TokenStatus.WAITING || t.getStatus() == TokenStatus.CALLED || t.getStatus() == TokenStatus.SERVING))
                    .findFirst();

            if (existingActive.isPresent()) {
                throw new ConflictException("You already have an active token (" + existingActive.get().getTokenNumber() + ") for this service.");
            }
        }

        // Today start of day in UTC/system
        Instant startOfDay = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant();
        int maxSeq = queueTokenRepository.findMaxSequenceForToday(service.getId(), startOfDay);
        int sequenceNumber = maxSeq + 1;

        // Unique formatted token number: e.g. "REG-A042" or "DEPT-PREF-001"
        String prefix = service.getPrefix() != null && !service.getPrefix().isBlank() ? service.getPrefix().trim() : "Q";
        String tokenNumber = String.format("%s-%s-%03d", department.getCode(), prefix, sequenceNumber);

        TokenPriority priority = req.getPriority() != null ? req.getPriority() : TokenPriority.REGULAR;

        // Calculate dynamic wait time based on people ahead and active counters
        long peopleAhead = queueTokenRepository.countWaitingInService(service.getId());
        int activeCounters = Math.max(1, counterRepository.findByDepartmentIdAndActiveTrue(department.getId()).size());
        int estimatedWait = (int) Math.max(2, ((peopleAhead + 1) * service.getEstimatedDurationMinutes()) / activeCounters);

        String customerName = req.getCustomerName() != null && !req.getCustomerName().isBlank()
                ? req.getCustomerName().trim()
                : (customerUser != null ? customerUser.getFullName() : "Walk-in Guest");

        String customerPhone = req.getCustomerPhone() != null && !req.getCustomerPhone().isBlank()
                ? req.getCustomerPhone().trim()
                : (customerUser != null ? customerUser.getPhoneNumber() : "");

        QueueToken token = new QueueToken(
                tokenNumber,
                organization,
                department,
                service,
                customerUser,
                customerName,
                customerPhone,
                priority,
                sequenceNumber,
                estimatedWait
        );

        token = queueTokenRepository.save(token);

        // Record audit history
        TokenHistory history = new TokenHistory(token, null, TokenStatus.WAITING, customerUser, "Token generated in queue");
        tokenHistoryRepository.save(history);

        TokenDto dto = enrichTokenDto(token);

        // Broadcast real-time WebSocket update
        wsPublisher.publishTokenUpdate(dto);

        return dto;
    }

    /**
     * Concurrency-safe Call Next Token for a Staff Counter.
     * Guarantees two staff members cannot process the same token simultaneously.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public synchronized TokenDto callNextToken(Long counterId, User staffUser) {
        ServiceCounter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new ResourceNotFoundException("Counter not found with ID: " + counterId));

        if (!Boolean.TRUE.equals(counter.getActive())) {
            throw new BadRequestException("Counter is currently inactive");
        }

        // Check if counter is already in middle of serving or calling a token
        var existingActive = queueTokenRepository.findByServiceCounterIdAndStatusInOrderByCreatedAtDesc(
                counterId, List.of(TokenStatus.CALLED, TokenStatus.SERVING)
        );
        if (!existingActive.isEmpty()) {
            throw new ConflictException("Counter " + counter.getCounterNumber() + " is currently occupied with token " +
                    existingActive.get(0).getTokenNumber() + ". Complete or mark as absent before calling next.");
        }

        // Fetch candidate waiting tokens for this department ordered by priority DESC, createdAt ASC
        List<QueueToken> waitingTokens = queueTokenRepository.findByDepartmentIdAndStatusOrderByPriorityDescCreatedAtAsc(
                counter.getDepartment().getId(), TokenStatus.WAITING
        );

        if (waitingTokens.isEmpty()) {
            throw new ResourceNotFoundException("No waiting customers in the queue for department " + counter.getDepartment().getName());
        }

        QueueToken calledToken = null;
        Instant now = Instant.now();

        // Atomic lock attempt through repository to strictly avoid race conditions
        for (QueueToken candidate : waitingTokens) {
            int updated = queueTokenRepository.atomicCallToken(candidate.getId(), counter.getId(), TokenStatus.CALLED, now);
            if (updated > 0) {
                // Successfully claimed by this counter
                candidate.setStatus(TokenStatus.CALLED);
                candidate.setServiceCounter(counter);
                candidate.setCalledAt(now);
                candidate.setRecallCount(candidate.getRecallCount() + 1);
                calledToken = candidate;
                break;
            }
        }

        if (calledToken == null) {
            throw new ConflictException("Another staff member just claimed the next token. Please try again.");
        }

        // Record history
        TokenHistory history = new TokenHistory(
                calledToken, TokenStatus.WAITING, TokenStatus.CALLED, staffUser,
                "Called to " + counter.getCounterNumber() + " by " + staffUser.getFullName()
        );
        tokenHistoryRepository.save(history);

        TokenDto dto = enrichTokenDto(calledToken);
        wsPublisher.publishTokenUpdate(dto);

        return dto;
    }

    /**
     * Recall a previously called customer to alert them again.
     */
    @Transactional
    public TokenDto recallToken(Long tokenId, Long counterId, User staffUser) {
        QueueToken token = queueTokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found with ID: " + tokenId));

        if (token.getStatus() != TokenStatus.CALLED && token.getStatus() != TokenStatus.SERVING) {
            throw new BadRequestException("Can only recall tokens that are in CALLED or SERVING status");
        }

        token.setCalledAt(Instant.now());
        token.setRecallCount(token.getRecallCount() + 1);
        token = queueTokenRepository.save(token);

        TokenHistory history = new TokenHistory(
                token, token.getStatus(), token.getStatus(), staffUser,
                "Recalled by staff (Notice #" + token.getRecallCount() + ")"
        );
        tokenHistoryRepository.save(history);

        TokenDto dto = enrichTokenDto(token);
        wsPublisher.publishTokenUpdate(dto);
        return dto;
    }

    /**
     * Transition token to SERVING once customer arrives at counter.
     */
    @Transactional
    public TokenDto startServing(Long tokenId, Long counterId, User staffUser) {
        QueueToken token = queueTokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found with ID: " + tokenId));

        if (token.getStatus() != TokenStatus.CALLED) {
            throw new BadRequestException("Token must be CALLED before starting service (Current status: " + token.getStatus() + ")");
        }

        TokenStatus previousStatus = token.getStatus();
        token.setStatus(TokenStatus.SERVING);
        token.setServedAt(Instant.now());
        token = queueTokenRepository.save(token);

        TokenHistory history = new TokenHistory(
                token, previousStatus, TokenStatus.SERVING, staffUser,
                "Service started at counter"
        );
        tokenHistoryRepository.save(history);

        TokenDto dto = enrichTokenDto(token);
        wsPublisher.publishTokenUpdate(dto);
        return dto;
    }

    /**
     * Mark service as completed successfully.
     */
    @Transactional
    public TokenDto completeService(Long tokenId, Long counterId, User staffUser, String notes) {
        QueueToken token = queueTokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found with ID: " + tokenId));

        if (token.getStatus() != TokenStatus.SERVING && token.getStatus() != TokenStatus.CALLED) {
            throw new BadRequestException("Cannot complete token in status " + token.getStatus());
        }

        TokenStatus previousStatus = token.getStatus();
        token.setStatus(TokenStatus.COMPLETED);
        token.setCompletedAt(Instant.now());
        token = queueTokenRepository.save(token);

        TokenHistory history = new TokenHistory(
                token, previousStatus, TokenStatus.COMPLETED, staffUser,
                notes != null && !notes.isBlank() ? notes : "Service successfully completed"
        );
        tokenHistoryRepository.save(history);

        TokenDto dto = enrichTokenDto(token);
        wsPublisher.publishTokenUpdate(dto);
        return dto;
    }

    /**
     * Mark customer as absent / NO_SHOW.
     */
    @Transactional
    public TokenDto markNoShow(Long tokenId, Long counterId, User staffUser, String notes) {
        QueueToken token = queueTokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found with ID: " + tokenId));

        if (token.getStatus() != TokenStatus.CALLED && token.getStatus() != TokenStatus.SERVING) {
            throw new BadRequestException("Cannot mark no-show for token in status " + token.getStatus());
        }

        TokenStatus previousStatus = token.getStatus();
        token.setStatus(TokenStatus.NO_SHOW);
        token.setCompletedAt(Instant.now());
        token = queueTokenRepository.save(token);

        TokenHistory history = new TokenHistory(
                token, previousStatus, TokenStatus.NO_SHOW, staffUser,
                notes != null && !notes.isBlank() ? notes : "Customer did not show up after repeated calls"
        );
        tokenHistoryRepository.save(history);

        TokenDto dto = enrichTokenDto(token);
        wsPublisher.publishTokenUpdate(dto);
        return dto;
    }

    /**
     * Cancel token by customer or admin.
     */
    @Transactional
    public TokenDto cancelToken(Long tokenId, User user, String notes) {
        QueueToken token = queueTokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found with ID: " + tokenId));

        if (token.getStatus() != TokenStatus.WAITING && token.getStatus() != TokenStatus.CALLED) {
            throw new BadRequestException("Cannot cancel a token that is already " + token.getStatus());
        }

        TokenStatus previousStatus = token.getStatus();
        token.setStatus(TokenStatus.CANCELLED);
        token.setCompletedAt(Instant.now());
        token = queueTokenRepository.save(token);

        TokenHistory history = new TokenHistory(
                token, previousStatus, TokenStatus.CANCELLED, user,
                notes != null && !notes.isBlank() ? notes : "Cancelled by user"
        );
        tokenHistoryRepository.save(history);

        TokenDto dto = enrichTokenDto(token);
        wsPublisher.publishTokenUpdate(dto);
        return dto;
    }

    /**
     * Customer query: Get all tokens for a customer.
     */
    @Transactional(readOnly = true)
    public List<TokenDto> getCustomerTokens(Long customerUserId) {
        return queueTokenRepository.findByCustomerUserIdOrderByCreatedAtDesc(customerUserId).stream()
                .map(this::enrichTokenDto)
                .collect(Collectors.toList());
    }

    /**
     * Customer query: Get single token status by ID.
     */
    @Transactional(readOnly = true)
    public TokenDto getTokenById(Long tokenId) {
        QueueToken token = queueTokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found with ID: " + tokenId));
        return enrichTokenDto(token);
    }

    /**
     * Staff query: Get active & waiting tokens for counter/department.
     */
    @Transactional(readOnly = true)
    public List<TokenDto> getWaitingTokensForDepartment(Long departmentId) {
        return queueTokenRepository.findByDepartmentIdAndStatusOrderByPriorityDescCreatedAtAsc(departmentId, TokenStatus.WAITING).stream()
                .map(this::enrichTokenDto)
                .collect(Collectors.toList());
    }

    /**
     * Staff query: Get current active token for counter (if any).
     */
    @Transactional(readOnly = true)
    public TokenDto getActiveTokenForCounter(Long counterId) {
        var active = queueTokenRepository.findByServiceCounterIdAndStatusInOrderByCreatedAtDesc(
                counterId, List.of(TokenStatus.CALLED, TokenStatus.SERVING)
        );
        return active.isEmpty() ? null : enrichTokenDto(active.get(0));
    }

    /**
     * Staff query: Get recently completed tokens for counter.
     */
    @Transactional(readOnly = true)
    public List<TokenDto> getCompletedTokensForCounter(Long counterId) {
        return queueTokenRepository.findByServiceCounterIdOrderByCreatedAtDesc(counterId, PageRequest.of(0, 15)).stream()
                .filter(t -> t.getStatus() == TokenStatus.COMPLETED || t.getStatus() == TokenStatus.NO_SHOW)
                .map(this::enrichTokenDto)
                .collect(Collectors.toList());
    }

    /**
     * Public Board query: Provides real-time view for large monitors in waiting halls.
     */
    @Transactional(readOnly = true)
    public PublicQueueBoardDto getPublicQueueBoard(Long organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + organizationId));

        PublicQueueBoardDto board = new PublicQueueBoardDto();
        board.setOrganizationId(org.getId());
        board.setOrganizationName(org.getName());

        // Currently calling / serving
        var activeTokens = queueTokenRepository.findByOrganizationIdAndStatusIn(
                organizationId, List.of(TokenStatus.CALLED, TokenStatus.SERVING)
        );
        board.setNowCalling(activeTokens.stream()
                .map(t -> new LiveQueueItemDto(
                        t.getId(), t.getTokenNumber(), t.getPriority(), t.getStatus(),
                        t.getService().getName(),
                        t.getServiceCounter() != null ? t.getServiceCounter().getCounterNumber() : "TBD",
                        t.getServiceCounter() != null ? t.getServiceCounter().getName() : "",
                        maskCustomerName(t.getCustomerName()),
                        t.getEstimatedWaitMinutes(),
                        0L
                ))
                .collect(Collectors.toList()));

        // Waiting in line
        var waitingTokens = queueTokenRepository.findByOrganizationIdAndStatusIn(
                organizationId, List.of(TokenStatus.WAITING)
        );
        board.setTotalWaiting(waitingTokens.size());
        board.setWaiting(waitingTokens.stream()
                .limit(12)
                .map(t -> new LiveQueueItemDto(
                        t.getId(), t.getTokenNumber(), t.getPriority(), t.getStatus(),
                        t.getService().getName(),
                        "-", "-",
                        maskCustomerName(t.getCustomerName()),
                        t.getEstimatedWaitMinutes(),
                        queueTokenRepository.countPeopleAhead(t.getService().getId(), t.getPriority(), t.getCreatedAt())
                ))
                .collect(Collectors.toList()));

        // Completed today
        var completed = queueTokenRepository.findByOrganizationIdAndStatusIn(
                organizationId, List.of(TokenStatus.COMPLETED)
        );
        board.setTotalServedToday(completed.size());
        board.setRecentlyCompleted(completed.stream()
                .limit(8)
                .map(t -> new LiveQueueItemDto(
                        t.getId(), t.getTokenNumber(), t.getPriority(), t.getStatus(),
                        t.getService().getName(),
                        t.getServiceCounter() != null ? t.getServiceCounter().getCounterNumber() : "-",
                        "", maskCustomerName(t.getCustomerName()),
                        0, 0L
                ))
                .collect(Collectors.toList()));

        return board;
    }

    private TokenDto enrichTokenDto(QueueToken token) {
        TokenDto dto = TokenDto.fromEntity(token);
        if (token.getStatus() == TokenStatus.WAITING) {
            long peopleAhead = queueTokenRepository.countPeopleAhead(
                    token.getService().getId(), token.getPriority(), token.getCreatedAt()
            );
            dto.setPeopleAhead(peopleAhead);
        } else {
            dto.setPeopleAhead(0L);
        }
        return dto;
    }

    private String maskCustomerName(String name) {
        if (name == null || name.isBlank()) return "Customer";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            if (parts[0].length() <= 2) return parts[0];
            return parts[0].substring(0, 2) + "***";
        }
        return parts[0] + " " + parts[parts.length - 1].charAt(0) + ".";
    }
}
